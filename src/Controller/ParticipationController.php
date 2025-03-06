<?php

namespace App\Controller;

use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use App\Entity\Participation;
use App\Entity\Formation;
use App\Form\ParticipationType;
use App\Repository\ParticipationRepository;
use Doctrine\Persistence\ManagerRegistry;
use Symfony\Component\HttpFoundation\Request;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\SecurityBundle\Security;
use TCPDF;



final class ParticipationController extends AbstractController
{
    private ManagerRegistry $doctrine;

    // Injection de ManagerRegistry via le constructeur
    public function __construct(ManagerRegistry $doctrine)
    {
        $this->doctrine = $doctrine;
    }
    


    #[Route('/participation', name: 'app_participation')]
    public function index(): Response
    {
        return $this->render('participation/index.html.twig', [
            'controller_name' => 'ParticipationController',
        ]);
    }

    #[Route('addParticipation', name: 'addParticipation')]
    public function addParticipation(Request $request, ParticipationRepository $participationRepository): Response
    {
        $participation = new Participation();
        $form = $this->createForm(ParticipationType::class, $participation);
        $form->handleRequest($request);
    
        // Récupérer l'utilisateur connecté
        $utilisateur = $this->getUser();
    
        // Vérifier si l'utilisateur est authentifié
        if (!$utilisateur) {
            $this->addFlash('error', 'Vous devez être connecté pour participer.');
            return $this->redirectToRoute('app_login'); // Redirigez vers la page de connexion si l'utilisateur n'est pas connecté
        }
    
        if ($form->isSubmitted() && $form->isValid()) {
            // Affecter l'utilisateur à la participation
            $participation->setUtilisateurs($utilisateur);
    
            // Utilisation de l'EntityManager via le ManagerRegistry
            $entityManager = $this->doctrine->getManager();
            $entityManager->persist($participation);
            $entityManager->flush();
    
            // Ajouter un message flash de succès
            $this->addFlash('success', 'Participation ajoutée avec succès !');
    
            // Rediriger vers la page des participations
            return $this->redirectToRoute('showMesParticipations');
        }
    
        return $this->render('formations/addParticipation.html.twig', [
            'form' => $form->createView(),
        ]);
    }
    


/*#[Route('/showParticipations', name: 'showParticipations')]
public function afficher(ParticipationRepository $participationRepository, Request $request): Response
{
    // Récupérer toutes les participations
    $participations = $participationRepository->findAll();

    // Récupérer la valeur de recherche depuis la requête
    $search = $request->query->get('search');

    // Filtrer les participations si un texte de recherche est fourni
    if ($search) {
        $participations = array_filter($participations, function ($participation) use ($search) {
            // Filtrer les participations en fonction du titre de la formation
            return stripos($participation->getFormation()->getTitre(), $search) !== false;
        });
    }

    return $this->render('formations/showParticipations.html.twig', [
        'participations' => $participations,
    ]);
}*/
#[Route('/showParticipations', name: 'showParticipations')]
public function afficher(ParticipationRepository $participationRepository, Request $request): Response
{
    // Récupérer toutes les participations
    $participations = $participationRepository->findAll();

    // Récupérer la valeur de recherche depuis la requête
    $search = $request->query->get('search');
    $count = null;

    // Si un texte de recherche est fourni
    if ($search) {
        // Filtrer les participations en fonction du titre de la formation
        $participations = array_filter($participations, function ($participation) use ($search) {
            return stripos($participation->getFormation()->getTitre(), $search) !== false;
        });

        // Compter le nombre de participants si un titre est donné
        $count = count($participations); // Nombre de participations correspondant à la recherche
    }

    return $this->render('formations/showParticipations.html.twig', [
        'participations' => $participations,
        'count' => $count,  // Passer le nombre de participants à la vue
        'search' => $search,  // Passer la valeur de recherche à la vue
    ]);
}



    #[Route('/deleteparticipation/{id}', name: 'deleteParticipation', methods: ['GET', 'POST'])]
    public function deleteParticipation($id, EntityManagerInterface $entityManager, ParticipationRepository $participationRepository): Response
    {
        $participation = $participationRepository->find($id);

        if (!$participation) {
            throw $this->createNotFoundException('Participation non trouvée');
        }

        $entityManager->remove($participation);
        $entityManager->flush();

        $this->addFlash('success', 'Participation supprimée avec succès !');

        return $this->redirectToRoute('showParticipations');
    }
    #[Route('/showMesParticipations', name: 'showMesParticipations')]
    public function showMesParticipations(ParticipationRepository $participationRepository, Security $security, Request $request): Response
    {
        // Récupérer l'utilisateur actuellement connecté
        $user = $security->getUser();
        
        if (!$user) {
            throw $this->createAccessDeniedException('Vous devez être connecté pour voir vos participations.');
        }
    
        // Récupérer la valeur de l'ordre de tri depuis la requête (par défaut 'asc')
        $order = $request->query->get('order', 'asc'); // Cela permet d'obtenir 'asc' ou 'desc'
    
        // Récupérer toutes les participations de l'utilisateur triées par date de formation
        $participations = $participationRepository->findByUserOrderedByFormationDate($user, $order);
    
        // Filtrer les participations pour ne garder que celles où la formation n'est pas expirée
        $validParticipations = [];
        $currentDate = new \DateTime();
        foreach ($participations as $participation) {
            if ($participation->getFormation()->getDate() >= $currentDate) {
                $validParticipations[] = $participation;
            }
        }
    
        // Débogage : vérifier les participations valides après filtrage
        dump($validParticipations); // Cette ligne permet de visualiser les participations valides
    
        return $this->render('formations/showMesParticipations.html.twig', [
            'user' => $user,
            'participations' => $validParticipations,
            'order' => $order,  // On passe aussi l'ordre au template
        ]);
    }
    
   #[Route('/participation/certificat/{id}', name: 'generate_certificate')]
public function generateCertificate(Participation $participation): Response
{
    // Créer un nouveau document PDF
    $pdf = new TCPDF();
    $pdf->SetCreator('Symfony');
    $pdf->SetAuthor('Votre Site');
    $pdf->SetTitle('Attestation de participation');
    $pdf->SetMargins(15, 15, 15);
    $pdf->AddPage();

    // Récupérer les chemins des images depuis services.yaml
    $logoPath = $this->getParameter('logo_path');
    $signaturePath = $this->getParameter('signature_path');

    // Ajouter le logo en haut à gauche (seulement si l'image existe)
    if (file_exists($logoPath)) {
        $pdf->Image($logoPath, 15, 10, 40); // X = 15, Y = 10, Taille = 40
    } else {
        error_log("⚠️ Logo introuvable : " . $logoPath);
    }

    // Ajouter "AgriWise" en haut à droite en vert
    $pdf->SetFont('helvetica', 'B', 12);
    $pdf->SetTextColor(0, 128, 0); // Vert
    $pdf->SetXY(150, 15); // Position X = 150 (à droite), Y = 15
    $pdf->Cell(0, 10, 'AgriWise', 0, 1, 'R'); // Aligné à droite (R)

    // Remettre la couleur du texte en noir pour la suite
    $pdf->SetTextColor(0, 0, 0);

    // Ligne de séparation sous l'entête
    $pdf->SetLineWidth(0.5);
    $pdf->Line(15, 50, 195, 50); // Ligne horizontale sous l'entête

    // Titre du certificat (bien positionné)
    $pdf->SetFont('helvetica', 'B', 16);
    $pdf->Ln(8); // Espace ajusté
    $pdf->Cell(0, 10, 'Attestation de participation', 0, 1, 'C');

    // Ajouter le titre de la formation
    $pdf->SetFont('helvetica', '', 14);
    $pdf->Ln(10);
    $pdf->Cell(0, 10, "Au programme: " . $participation->getFormation()->getTitre(), 0, 1, 'C');

    // Ajouter la date de la formation
    $pdf->Ln(5);
    $pdf->SetFont('helvetica', '', 12);
    $pdf->Cell(0, 10, "Date de la formation: " . $participation->getFormation()->getDate()->format('d/m/Y'), 0, 1, 'C');

    // Ajouter les lignes d'appréciation
    $pdf->Ln(10);
    $pdf->MultiCell(0, 10, "Nous certifions que " . $participation->getUtilisateurs()->getNom() . " " . $participation->getUtilisateurs()->getPrenom() . " a participé activement à cette formation.", 0, 'C');

    $pdf->Ln(5);
    $pdf->MultiCell(0, 10, "Nous le félicitons pour son engagement et sa motivation.", 0, 'C');

    // Ajouter "Signature du responsable" au-dessus de la signature
    $pdf->SetFont('helvetica', 'B', 12);
    $pdf->SetXY(20, 190); // Position X = 20 (à gauche), Y = 190 (remonté)
    $pdf->Cell(0, 10, "Signature du responsable", 0, 1, 'L'); // Aligné à gauche

    // Ajouter la signature en dessous de la phrase
    if (file_exists($signaturePath)) {
        $pdf->Image($signaturePath, 20, 200, 50); // X = 20 (à gauche), Y = 200 (remonté), Taille = 50
    } else {
        error_log("⚠️ Signature introuvable : " . $signaturePath);
    }

    // Générer le PDF et l'envoyer au navigateur
    return new Response($pdf->Output('attestation.pdf', 'I'), 200, [
        'Content-Type' => 'application/pdf',
    ]);
}


    
    
}

  /*  #[Route('showMesParticipations', name: 'showMesParticipations')]
    public function showMesParticipations(ParticipationRepository $participationRepository): Response
    {
        // Récupérer l'utilisateur connecté
        $user = $this->getUser();
    
        // Trouver toutes les participations de cet utilisateur
        $participations = $participationRepository->findBy(['user' => $user]);
    
        // Filtrer les participations pour ne garder que celles où la formation n'est pas expirée
        $validParticipations = [];
        $currentDate = new \DateTime();
        foreach ($participations as $participation) {
            if ($participation->getFormation()->getDate() >= $currentDate) {
                $validParticipations[] = $participation;
            } else {
                // Supprimer la participation si la formation est expirée
                $entityManager = $this->doctrine->getManager();
                $entityManager->remove($participation);
                $entityManager->flush();
            }
        }
    
        // Renvoyer la vue avec les participations valides

        return $this->render('formations/showMesParticipations.html.twig', [
            'user' => $user,  // Ajout de la variable user
            'participations' => $validParticipations,
        ]);
    }
    

/* #[Route('/participer/{id}', name: 'participer_formation')]
public function participerFormation(int $id, EntityManagerInterface $entityManager): Response
{
    // Récupérer l'utilisateur connecté
    $user = $this->getUser();

    // Check if the user is authenticated
    if (!$user) {
        throw $this->createAccessDeniedException('You must be logged in to participate in a formation.');
    }

    // Trouver la formation par son ID
    $formation = $entityManager->getRepository(Formation::class)->find($id);

    if (!$formation) {
        throw $this->createNotFoundException('Formation non trouvée');
    }

    // Vérifier si l'utilisateur est déjà inscrit à cette formation
    $existingParticipation = $entityManager->getRepository(Participation::class)->findOneBy([
        'user' => $user,
        'formation' => $formation,
    ]);

    if (!$existingParticipation) {
        // Créer une nouvelle participation
        $participation = new Participation();
        $participation->setUser($user);
        $participation->setFormation($formation);

        // Enregistrer la participation
        $entityManager->persist($participation);
        $entityManager->flush();
    }

    // Rediriger vers les participations de l'utilisateur
    return $this->redirectToRoute('showMesParticipations');
}*/
/*#[Route('showParticipationsByUser/{id}', name: 'showParticipationsByUser')]
public function showParticipationsByUser($id, ParticipationRepository $participationRepository): Response
{
    // Tableau des utilisateurs de test (id => nom)
    $users = [
        1 => 'User 1',
        2 => 'User 2',
        3 => 'User 3', // Ajoutez d'autres utilisateurs pour les tests
    ];

    // Vérifier si l'utilisateur existe dans le tableau de test
    if (!isset($users[$id])) {
        throw $this->createNotFoundException('Utilisateur non trouvé.');
    }

    // Récupérer les participations de l'utilisateur avec l'ID fourni
    $participations = $participationRepository->findBy(['user' => $id]);

    // Filtrer les participations pour ne garder que celles où la formation n'est pas expirée
    $validParticipations = [];
    $currentDate = new \DateTime();
    foreach ($participations as $participation) {
        if ($participation->getFormation()->getDate() >= $currentDate) {
            $validParticipations[] = $participation;
        }
    }

    // Renvoyer la vue avec les participations valides
    return $this->render('formations/showMesParticipations.html.twig', [
        'user' => $users[$id],
        'participations' => $validParticipations,
    ]);
}
/*#[Route('/participer/{id}', name: 'participer_formation')]
public function participerFormation(int $id, EntityManagerInterface $entityManager, ParticipationRepository $participationRepository): Response
{
    // Récupérer l'utilisateur connecté
    $user = $this->getUser();
    if (!$user) {
        throw $this->createAccessDeniedException('Vous devez être connecté pour participer à une formation.');
    }

    // Trouver la formation
    $formation = $entityManager->getRepository(Formation::class)->find($id);
    if (!$formation) {
        throw $this->createNotFoundException('Formation non trouvée');
    }

    // Vérifier si la formation est expirée
    $currentDate = new \DateTime();
    if ($formation->getDate() < $currentDate) {
        $this->addFlash('danger', 'Cette formation est expirée.');
        return $this->redirectToRoute('showMesParticipations');
    }

    // Vérifier si l'utilisateur est déjà inscrit
    $existingParticipation = $participationRepository->findOneBy([
        'user' => $user,
        'formation' => $formation,
    ]);

    if (!$existingParticipation) {
        // Ajouter la participation
        $participation = new Participation();
        $participation->setUser($user);
        $participation->setFormation($formation);

        $entityManager->persist($participation);
        $entityManager->flush();

        $this->addFlash('success', 'Vous êtes inscrit à la formation !');
    } else {
        $this->addFlash('info', 'Vous êtes déjà inscrit à cette formation.');
    }

    return $this->redirectToRoute('showMesParticipations');
}
*/




