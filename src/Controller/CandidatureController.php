<?php

namespace App\Controller;

use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\Routing\Attribute\Route;
use App\Repository\CandidatureRepository;
use App\Repository\UtilisateursRepository;
use App\Form\CandidatureType;
use Doctrine\ORM\EntityManagerInterface;
use Doctrine\Persistence\ManagerRegistry;
use Symfony\Component\Validator\Validator\ValidatorInterface;
use Symfony\Bundle\SecurityBundle\Security;
use Symfony\Component\Security\Core\Authentication\Token\Storage\TokenStorageInterface;
use App\Repository\TerrainRepository;
use App\Entity\Candidature;
use App\Entity\Terrain;
use Symfony\Component\Mime\Email;
use Symfony\Component\Mailer\MailerInterface;
use Symfony\Component\Mime\Address;
use Symfony\Component\Serializer\Normalizer\NormalizerInterface;
use Symfony\Bridge\Twig\Mime\TemplatedEmail;
use App\Service\OpenAIService;
use Symfony\Component\Routing\Generator\UrlGeneratorInterface;






class CandidatureController extends AbstractController
{
    private $security;
    private $entityManager;
    private $openAIService;
    private $urlGenerator;

    public function __construct(Security $security, EntityManagerInterface $entityManager, OpenAIService $openAIService,UrlGeneratorInterface $urlGenerator)
    {
        $this->security = $security;
        $this->entityManager = $entityManager;
        $this->openAIService = $openAIService;
        $this->urlGenerator = $urlGenerator;
    }

    // Route pour obtenir des conseils et les enregistrer dans la candidature
    #[Route('/candidature/conseils/{id}', name: 'candidature_conseils')]
    public function getConseils(int $id): Response
    {
        // Récupérer la candidature par son ID
        $candidature = $this->entityManager->getRepository(Candidature::class)->find($id);

        // Si la candidature n'existe pas
        if (!$candidature) {
            return new Response('Candidature non trouvée', 404);
        }

        // Récupérer le but de la candidature
        $but = $candidature->getBut();

        // Si le but est vide
        if (!$but) {
            return new Response('Le but du candidat est vide.', 400);
        }

        // Obtenir des recommandations via OpenAI
        $recommandation = $this->openAIService->getRecommandation($but);

        // Sauvegarder la recommandation dans la candidature
        $candidature->setRecommandation($recommandation);
        $this->entityManager->flush();

        // Retourner la réponse avec les recommandations
        return new Response("Conseils enregistrés : " . $recommandation);
    }
    #[Route('/candidature', name: 'app_candidature')]
    public function index(): Response
    {
        return $this->render('candidature/index.html.twig', [
            'controller_name' => 'CandidatureController',
        ]);
    }

    //SHOW
    #[Route('/showcandidature', name: 'app_showcandidature')]
   
    public function showcandidature(CandidatureRepository $a): Response
    {
        // Récupérer l'utilisateur connecté
        $user = $this->getUser();
    
        if (!$user) {
            throw $this->createAccessDeniedException("Vous devez être connecté pour voir vos candidatures.");
        }
    
        // Récupérer les candidatures de l'utilisateur connecté
        $candidatures = $a->findBy(['utilisateur' => $user]);
    
        return $this->render('candidature/showcandidature.html.twig', [
            'tab_candidature' => $candidatures,
        ]);
    }
    
    
    

 // Ajouter la méthode logHistory
 private function logHistory(string $action, array $details): void
 {
     // Récupérer l'utilisateur actuel
     $user = $this->security->getUser();

     // Chemin du fichier d'historique
     $filePath = __DIR__ . '/../../public/histories/historique.txt';

     // Préparer l'entrée de l'historique avec la date, l'heure et l'utilisateur
     $dateTime = new \DateTime();
     $logEntry = sprintf(
         "[%s] [Utilisateur ID: %d] [Action: %s] [Détails: %s]\n",
         $dateTime->format('Y-m-d H:i:s'),
         $user ? $user->getId() : 'Anonymous',
         $action,
         json_encode($details)
     );

     // Écrire dans le fichier
     file_put_contents($filePath, $logEntry, FILE_APPEND);
 }

 // ... (autres méthodes)

 // AJOUT
 #[Route('/addformcandidature/{idTerrain}', name: 'app_addformcandidature')]
 public function ajouterCandidature(
     Request $request, 
     EntityManagerInterface $entityManager, 
     TerrainRepository $terrainRepository, 
     int $idTerrain
 ) {
     // Récupérer l'objet terrain à partir de l'ID
     $terrain = $entityManager->getRepository(Terrain::class)->find($idTerrain);
     
     // Vérifier si le terrain existe
     if (!$terrain) {
         throw $this->createNotFoundException('Terrain non trouvé');
     }

     // Créer une nouvelle candidature
     $candidature = new Candidature();
     $candidature->setIdTerrain($terrain);
     $candidature->setMontant($terrain->getPrix());

     // Récupérer l'utilisateur connecté
     $user = $this->getUser();
     if (!$user) {
         throw $this->createAccessDeniedException('Vous devez être connecté pour postuler.');
     }

     // Associer l'utilisateur à la candidature
     $candidature->setUtilisateur($user);

     // Créer le formulaire
     $form = $this->createForm(CandidatureType::class, $candidature, [
         'terrains' => $terrainRepository->findAll(),
     ]);

     $form->handleRequest($request);

     if ($form->isSubmitted() && $form->isValid()) {
         // Persister la candidature avec l'utilisateur associé
         $entityManager->persist($candidature);
         $entityManager->flush();

         // Enregistrer l'historique
         $this->logHistory('Ajout de candidature', [
             'candidature_id' => $candidature->getId(),
             'terrain_id' => $terrain->getId(),
             'utilisateur_id' => $user->getId(),
         ]);

         // Rediriger vers la page des candidatures
         return $this->redirectToRoute('app_showcandidature');
     }

     // Rendre la vue avec les variables nécessaires
     return $this->render('candidature/addformcandidature.html.twig', [
         'formadd' => $form->createView(),
         'terrain' => $terrain,
     ]);
 }

 // DELETE
 #[Route('/candidature/delete/{id}', name: 'app_deletecandidature')]
 public function deletecandidature(int $id, EntityManagerInterface $entityManager): Response
 {
     $candidature = $entityManager->getRepository(Candidature::class)->find($id);

     if (!$candidature) {
         throw $this->createNotFoundException('Candidature non trouvée');
     }

     // Enregistrer l'historique avant la suppression
     $this->logHistory('Suppression de candidature', [
         'candidature_id' => $candidature->getId(),
         'terrain_id' => $candidature->getIdTerrain()->getId(),
         'utilisateur_id' => $candidature->getUtilisateur()->getId(),
     ]);

     // Supprimer la candidature
     $entityManager->remove($candidature);
     $entityManager->flush();

     return $this->redirectToRoute('app_showcandidature');
 }

    


//UPDATE FROM FORMULAIRE

#[Route('/updateformcandidature/{id}', name: 'app_updateformcandidature')]
public function updateformcandidature(ManagerRegistry $m, Request $req, $id, CandidatureRepository $rep): Response
{
    $em = $m->getManager();
    $candidature = $rep->find($id);

    // Sauvegarder les anciennes valeurs pour les champs que vous ne voulez pas modifier
    $oldMontant = $candidature->getMontant();
    $oldIdTerrain = $candidature->getIdTerrain();

    // Créer le formulaire et y passer les terrains
    $form = $this->createForm(CandidatureType::class, $candidature, [
        'terrains' => $m->getRepository(Terrain::class)->findAll(),
    ]);

    $form->handleRequest($req);
    
    if ($form->isSubmitted() && $form->isValid()) {
        // Remettre les anciennes valeurs pour les champs non modifiés
        $candidature->setMontant($oldMontant);
        $candidature->setIdTerrain($oldIdTerrain);

        $em->persist($candidature);
        $em->flush();

        return $this->redirectToRoute('app_showcandidature');
    }

    return $this->render('candidature/addformcandidature.html.twig', [
        'formadd' => $form->createView(),
    ]);
}


#[Route('/showcandidatures', name: 'app_showcandidatures')]
    public function showCandidatures(CandidatureRepository $candidatureRepository): Response
    {
        // Récupérer toutes les candidatures
        $candidatures = $candidatureRepository->findAll();

        // Retourner la vue avec les candidatures
        return $this->render('candidature/show_all_candidatures.html.twig', [
            'candidatures' => $candidatures,
        ]);
    }

    #[Route('/mes-candidatures', name: 'app_candidature_mes_candidatures')]
    public function mesCandidatures(CandidatureRepository $candidatureRepository, Security $security): Response
    {
        $user = $security->getUser(); // Récupère l'utilisateur connecté

        if (!$user) {
            throw $this->createAccessDeniedException('Vous devez être connecté pour voir vos candidatures.');
        }

        // Récupérer les candidatures liées aux terrains de l'utilisateur connecté
        $candidatures = $candidatureRepository->findByAgriculteur($user);

        return $this->render('candidature/mes_candidatures.html.twig', [
            'candidatures' => $candidatures,
        ]);
    }
   

// Accepter la candidature

#[Route('/candidature/{id}/accepter', name: 'candidature_accept')]
public function accepterCandidature(
    $id,
    MailerInterface $mailer,
    CandidatureRepository $candidatureRepo,
    UtilisateursRepository $userRepo,
    NormalizerInterface $Normalizer,
    EntityManagerInterface $entityManager,
    UrlGeneratorInterface $urlGenerator 
): Response {
    // Récupérer la candidature
    $candidature = $candidatureRepo->find($id);
    if (!$candidature) {
        return new Response("Candidature non trouvée", Response::HTTP_NOT_FOUND);
    }

    // Mettre à jour l'état de la candidature
    $candidature->setEtat('acceptée');
    $entityManager->flush();
    

    // Récupérer l'utilisateur associé à la candidature
    $utilisateur = $userRepo->find($candidature->getUtilisateur());

    // Récupérer les détails du terrain
    $terrain = $candidature->getIdTerrain(); // Supposons que la candidature est liée à un terrain
    $localisation = $terrain->getLocalisation();
    $superficie = $terrain->getSuperficie();
    $prix = $terrain->getPrix();
    // Générer l'URL vers la page "Mes Candidatures"
    $urlMesCandidatures = $urlGenerator->generate('app_candidature_mes_candidatures', [], UrlGeneratorInterface::ABSOLUTE_URL);
    // Envoi de l'email
    $message = (new TemplatedEmail())
        ->from(new Address('Agriwise@gmail.com', 'AgriWise'))
        ->to($utilisateur->getEmail())
        ->subject('Candidature acceptée')
        ->html("<p>Bonjour {$utilisateur->getNom()},</p>
                <p>Félicitations ! Votre candidature pour le terrain situé à <strong>{$localisation}</strong>, d'une superficie de <strong>{$superficie} m²</strong> et d'un prix de <strong>{$prix} TND</strong>, a été acceptée.</p>
                <p>Veuillez nous contacter pour les procédures du contrat.</p>
                <p>Merci pour votre confiance.</p>");

    $mailer->send($message);

    // Retourner une réponse JSON avec les informations de la candidature
    $jsonContent = $Normalizer->normalize($candidature, 'json', ['groups' => 'post:read']);
    return new Response(json_encode($jsonContent), Response::HTTP_OK);
    
}

    
#[Route('/candidature/{id}/refuser', name: 'candidature_reject')]
public function refuserCandidature(
    $id,
    MailerInterface $mailer,
    CandidatureRepository $candidatureRepo,
    UtilisateursRepository $userRepo,
    NormalizerInterface $Normalizer,
    EntityManagerInterface $entityManager
): Response {
    // Récupérer la candidature
    $candidature = $candidatureRepo->find($id);
    if (!$candidature) {
        return new Response("Candidature non trouvée", Response::HTTP_NOT_FOUND);
    }

    // Mettre à jour l'état de la candidature
    $candidature->setEtat('rejetée');
    $entityManager->flush();

    // Récupérer l'utilisateur associé à la candidature
    $utilisateur = $userRepo->find($candidature->getUtilisateur());

    // Récupérer les détails du terrain
    $terrain = $candidature->getIdTerrain(); // Supposons que la candidature est liée à un terrain
    $localisation = $terrain->getLocalisation();
    $superficie = $terrain->getSuperficie();
    $prix = $terrain->getPrix();

    // Envoi de l'email
    $message = (new TemplatedEmail())
        ->from(new Address('Agriwise@gmail.com', 'AgriWise'))
        ->to($utilisateur->getEmail())
        ->subject('Candidature refusée')
        ->html("<p>Bonjour {$utilisateur->getNom()},</p>
                <p>Nous sommes désolés, mais votre candidature pour le terrain situé à <strong>{$localisation}</strong>, d'une superficie de <strong>{$superficie} m²</strong> et d'un prix de <strong>{$prix} TND</strong>, a été refusée.</p>
                <p>Nous vous remercions de votre intérêt et vous encourageons à postuler à nouveau dans le futur.</p>
                <p>Merci pour votre compréhension.</p>");

    $mailer->send($message);

    // Retourner une réponse JSON avec les informations de la candidature
    $jsonContent = $Normalizer->normalize($candidature, 'json', ['groups' => 'post:read']);
    return new Response(json_encode($jsonContent), Response::HTTP_OK);
}

    #[Route('/candidature/{id}/update/{etat}', name: 'update_candidature', methods: ['POST'])]
    public function updateCandidature(int $id, string $etat, EntityManagerInterface $entityManager, MailerInterface $mailer): Response
    {
        // Récupérer la candidature
        $candidature = $entityManager->getRepository(Candidature::class)->find($id);
    
        if (!$candidature) {
            return $this->json(['success' => false, 'message' => 'Candidature non trouvée.']);
        }
    
        // Modifier l'état de la candidature
        if (in_array($etat, ['acceptée', 'rejetée'])) {
            $candidature->setEtat($etat);
            $entityManager->persist($candidature);
            $entityManager->flush();
    
            // Envoi de l'email
            $user = $candidature->getUtilisateur();
            $email = new Email();
            $email->from('amounatahfouna443@gmail.com')  // Remplace par ton adresse email
                ->to($user->getEmail())
                ->subject('Mise à jour de votre candidature')
                ->html('<p>Votre candidature pour le terrain "' . $candidature->getIdTerrain()->getDescription() . '" a été ' . $etat . '.</p>');
            
            $mailer->send($email);
    
            return $this->json(['success' => true, 'message' => 'Candidature mise à jour et email envoyé avec succès.']);
        }
    
        return $this->json(['success' => false, 'message' => 'État invalide.']);
    }

    //RECOMMANDATION

    #[Route('/candidature/recommendation/{id}', name: 'app_recommendation')]
    public function recommendation(int $id): Response
    {
        $candidature = $this->entityManager->getRepository(Candidature::class)->find($id);
    
        if (!$candidature) {
            throw $this->createNotFoundException('Candidature non trouvée.');
        }
    
        $but = $candidature->getBut();
    
        if (!$but) {
            return new Response('Le but du candidat est vide.', 400);
        }
    
        $recommandation = $this->openAIService->getRecommandation($but);
    
        if (!$recommandation) {
            $recommandation = "Désolé, aucune recommandation n'a pu être générée pour le moment.";
        }
    
        return $this->render('candidature/recommendation.html.twig', [
            'candidature' => $candidature,
            'recommandation' => $recommandation,
        ]);
    }

   
   

    
    
    
    
    
    
}



