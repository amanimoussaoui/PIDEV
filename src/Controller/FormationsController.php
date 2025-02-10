<?php

namespace App\Controller;

use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use App\Entity\Formation;
use App\Form\FormationType;
use App\Repository\FormationRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\File\Exception\FileException;
use Doctrine\Persistence\ManagerRegistry;
final class FormationsController extends AbstractController
{
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

    #[Route('/', name: 'formation_list', methods: ['GET'])]
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
    #[Route('/deleteformation/{id}', name: 'deleteFormation', methods: ['GET', 'POST'])]
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
    }
    #[Route('/formationsFront', name: 'formation_front_list', methods: ['GET'])]
public function showFormationsFront(EntityManagerInterface $entityManager): Response
{
    $formations = $entityManager->getRepository(Formation::class)->findAll();

    return $this->render('formations/showFormationsFront.html.twig', [
        'formations' => $formations,
    ]);
}


    
}
