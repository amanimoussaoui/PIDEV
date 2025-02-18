<?php

namespace App\Controller;

use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use App\Entity\Participation;
use App\Entity\Formation;
use App\Entity\User;
use App\Form\ParticipationType;
use App\Repository\ParticipationRepository;
use Doctrine\Persistence\ManagerRegistry;
use Symfony\Component\HttpFoundation\Request;
use Doctrine\ORM\EntityManagerInterface;


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

        if ($form->isSubmitted() && $form->isValid()) {
            // Utilisation de l'EntityManager via le ManagerRegistry
            $entityManager = $this->doctrine->getManager();
            $entityManager->persist($participation);
            $entityManager->flush();

            $this->addFlash('success', 'Participation ajoutée avec succès !');
            return $this->redirectToRoute('showMesParticipations');
        }

        return $this->render('formations/addParticipation.html.twig', [
            'form' => $form->createView(),
        ]);
    }

    #[Route('showParticipations', name: 'showParticipations')]
    public function afficher(ParticipationRepository $participationRepository): Response
    {
        $participations = $participationRepository->findAll();
        return $this->render('formations/showParticipations.html.twig', [
            'participations' => $participations,
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
    #[Route('showMesParticipations', name: 'showMesParticipations')]
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
#[Route('showParticipationsByUser/{id}', name: 'showParticipationsByUser')]
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



}
