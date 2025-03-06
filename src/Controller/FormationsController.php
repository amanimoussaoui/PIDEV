<?php

namespace App\Controller;

use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use App\Entity\Formation;
use App\Entity\Participation;
use App\Service\EmailService; 
use App\Form\FormationType;
use App\Repository\FormationRepository;
use App\Repository\ParticipationRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\File\Exception\FileException;
use Symfony\Component\Mailer\MailerInterface;
use Symfony\Component\Mime\Email;
use Doctrine\Persistence\ManagerRegistry;
use Symfony\Component\HttpFoundation\RedirectResponse;
final class FormationsController extends AbstractController


{
    private $emailService;
    private $entityManager;

    public function __construct(EmailService $emailService, EntityManagerInterface $entityManager)
    {
        $this->emailService = $emailService;
        $this->entityManager = $entityManager;
    }
    #[Route('/formations', name: 'app_formations')]
    public function index(): Response
    {
        return $this->render('formations/index.html.twig', [
            'controller_name' => 'FormationsController',
        ]);
    }

    #[Route('/addFormation', name: 'addFormation', methods: ['GET', 'POST'])]
    public function addFormation(Request $request, EntityManagerInterface $entityManager): Response
    {
        // Create a new Formation object
        $formation = new Formation();
        
        // Create and handle the form
        $form = $this->createForm(FormationType::class, $formation);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            // Persist the Formation entity in the database
            $entityManager->persist($formation);
            $entityManager->flush();

            // Add a success message
            $this->addFlash('success', 'Formation ajoutée avec succès !');

            // Redirect to the list of formations
            return $this->redirectToRoute('formation_list');
        }

        // Render the form view
        return $this->render('formations/addFormation.html.twig', [
            'form' => $form->createView(),
        ]);
    }

    #[Route('/listformationsBack', name: 'formation_list', methods: ['GET'])]
    public function showFormations(EntityManagerInterface $entityManager): Response
    {
        // Fetch all formations from the database
        $formations = $entityManager->getRepository(Formation::class)->findAll();

        // Render the formations list view
        return $this->render('formations/showFormations.html.twig', [
            'formations' => $formations,
        ]);
    }
    #[Route('/updateformation/{id}', name: 'updateFormation')]
    public function updateFormation(ManagerRegistry $m, FormationRepository $formationRepo, Request $req, $id): Response
    {
        $em = $m->getManager();
        $formation = $formationRepo->find($id);
    
        if (!$formation) {
            throw $this->createNotFoundException('Formation non trouvée');
        }
    
        $form = $this->createForm(FormationType::class, $formation);
        $form->handleRequest($req);
    
        if ($form->isSubmitted() && $form->isValid()) {
            $em->persist($formation);
            $em->flush();
    
            $this->addFlash('success', 'Formation mise à jour avec succès !');
    
            return $this->redirectToRoute('formation_list');
        }
    
        return $this->render('formations/addFormation.html.twig', [
            'form' => $form->createView(),
        ]);
    }
    /*#[Route('/deleteformation/{id}', name: 'deleteFormation', methods: ['GET', 'POST'])]
    public function deleteFormation($id, EntityManagerInterface $entityManager, FormationRepository $formationRepository): Response
    {
        $formation = $formationRepository->find($id);
    
        if (!$formation) {
            throw $this->createNotFoundException('Formation non trouvée');
        }
    
        $entityManager->remove($formation);
        $entityManager->flush();
    
        $this->addFlash('success', 'Formation supprimée avec succès !');
    
        return $this->redirectToRoute('formation_list');
    }*/
    #[Route('/deleteformation/{id}', name: 'deleteFormation', methods: ['GET', 'POST'])]
public function deleteFormation(
    $id,
    EntityManagerInterface $entityManager,
    FormationRepository $formationRepository,
    ParticipationRepository $participationRepository
): Response {
    // Récupérer la formation
    $formation = $formationRepository->find($id);

    if (!$formation) {
        throw $this->createNotFoundException('Formation non trouvée');
    }

    // Récupérer les participations liées à cette formation
    $participations = $participationRepository->findBy(['formation' => $formation]);

    // Envoyer un e-mail à chaque participant
    foreach ($participations as $participation) {
        $user = $participation->getUtilisateurs();
        if ($user && $user->getEmail()) {
            $subject = 'Formation annulée : ' . $formation->getTitre();
            $body = sprintf(
                '<p>Bonjour %s,</p>
                 <p>Nous vous informons que la formation "<strong>%s</strong>" a été annulée.</p>
                 <p>Cordialement,</p>
                 <p>L\'équipe AgriWise</p>',
                htmlspecialchars($user->getNom(), ENT_QUOTES, 'UTF-8'),
                htmlspecialchars($formation->getTitre(), ENT_QUOTES, 'UTF-8')
            );

            try {
                $this->emailService->sendEmail($user->getEmail(), $subject, $body);
            } catch (\Exception $e) {
                // Log l'erreur si l'envoi d'e-mail échoue
                error_log("Erreur lors de l'envoi de l'e-mail à " . $user->getEmail() . " : " . $e->getMessage());
            }
        }
    }

    // Supprimer la formation
    $entityManager->remove($formation);
    $entityManager->flush();

    // Ajouter un message flash et rediriger
    $this->addFlash('success', 'Formation supprimée avec succès et notifications envoyées !');
    return $this->redirectToRoute('formation_list');
}
    /**
 * @Route("/formations/delete/{id}", name="delete_formation")
 */
#[Route('/test-email', name: 'test_email')]
public function testEmail(EmailService $emailService): Response
{
    $to = 'tasnimsdiri2001@gmail.com'; // Remplacez par une adresse e-mail valide
    $subject = 'Test d\'envoi d\'e-mail';
    $template = 'formations/formation_annulee.html.twig';
    $context = [
        'user' => ['nom' => 'Test User'],
        'formation' => ['titre' => 'Formation de test'],
    ];

    try {
        $emailService->sendEmail($to, $subject, $template, $context);
        return new Response('E-mail envoyé avec succès !');
    } catch (\Exception $e) {
        return new Response('Erreur lors de l\'envoi de l\'e-mail : ' . $e->getMessage());
    }
}




   /* #[Route('/test-email', name: 'test_email')]
public function testEmail(MailerInterface $mailer): Response
{
    $email = (new Email())
        ->from('tasnimsdiri2001@gmail.com')
        ->to('slahsdiri1964@gmail.com')  // Teste avec ton propre e-mail
        ->subject('Test Symfony Mailer')
        ->text('Ceci est un e-mail de test envoyé depuis Symfony.')
        ->html('<p>Ceci est un <strong>e-mail de test</strong> envoyé depuis Symfony.</p>');

    try {
        $mailer->send($email);
        return new Response('E-mail envoyé avec succès!');
    } catch (\Exception $e) {
        return new Response('Erreur lors de l\'envoi de l\'e-mail: ' . $e->getMessage());
    }
}*/
 




/*#[Route('/deleteformation/{id}', name: 'deleteFormation', methods: ['GET', 'POST'])]
public function deleteFormation(
    int $id, 
    EntityManagerInterface $entityManager, 
    FormationRepository $formationRepository, 
    MailerInterface $mailer
): Response {
    // Récupérer la formation
    $formation = $formationRepository->find($id);
    if (!$formation) {
        throw $this->createNotFoundException('Formation non trouvée');
    }

    // Récupérer les participants
    $participants = $formation->getParticipations(); 

    // Envoi des e-mails de notification
    foreach ($participants as $participation) {
        $user = $participation->getUtilisateurs(); // Assure-toi que c'est correct
        if ($user && filter_var($user->getEmail(), FILTER_VALIDATE_EMAIL)) {
            $email = (new Email())
                ->from('tasnimsdiri2001@gmail.com')
                ->to($user->getEmail())
                ->subject('Notification : Formation annulée')
                ->html(sprintf(
                    '<p>Bonjour %s,</p><p>Nous vous informons que la formation "<strong>%s</strong>" a été annulée.</p>',
                    htmlspecialchars($user->getNom(), ENT_QUOTES, 'UTF-8'), 
                    htmlspecialchars($formation->getTitre(), ENT_QUOTES, 'UTF-8')
                ));
            try {
                $mailer->send($email);
            } catch (\Exception $e) {
                error_log("Erreur lors de l'envoi de l'e-mail à " . $user->getEmail() . " : " . $e->getMessage());

            }
        }
    }

    // Supprimer la formation après l'envoi des notifications
    $entityManager->remove($formation);
    $entityManager->flush();

    // Ajouter un message flash et rediriger
    $this->addFlash('success', 'Formation supprimée avec succès et notifications envoyées !');
    return $this->redirectToRoute('formation_list');
}*/

    #[Route('/formationsFront', name: 'formation_front_list', methods: ['GET'])]
public function showFormationsFront(EntityManagerInterface $entityManager): Response
{
    $formations = $entityManager->getRepository(Formation::class)->findAll();

    return $this->render('formations/showFormationsFront.html.twig', [
        'formations' => $formations,
    ]);
}

#[Route('/formation/{id}', name: 'formation_detail', methods: ['GET'])]
public function showFormationDetails($id, FormationRepository $formationRepository): Response
{
    // Fetch the formation by its ID
    $formation = $formationRepository->find($id);

    // If the formation is not found, throw an exception
    if (!$formation) {
        throw $this->createNotFoundException('Formation non trouvée');
    }

    // Render the formation details page
    return $this->render('formations/formationDetails.html.twig', [
        'formation' => $formation,
    ]);
}
#[Route('/formations/recherche', name: 'formation_recherche', methods: ['GET'])]
public function rechercher(FormationRepository $formationRepository, Request $request): Response
{
    $query = $request->query->get('query', '');
    $formations = [];

    if (!empty($query)) {
        $formations = $formationRepository->createQueryBuilder('f')
            ->where('f.titre LIKE :query OR f.prix LIKE :query')
            ->setParameter('query', '%' . $query . '%')
            ->getQuery()
            ->getResult();
    }

    return $this->render('formations/recherche.html.twig', [
        'formations' => $formations,
        'query' => $query,
    ]);
}
}


    

