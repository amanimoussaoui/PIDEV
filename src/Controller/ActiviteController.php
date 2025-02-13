<?php

namespace App\Controller;

use App\Entity\Activite;
use App\Form\ActiviteType;
use App\Form\SearchActiviteType;
use App\Repository\ActiviteRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

#[Route('/activite')]
final class ActiviteController extends AbstractController
{
    #[Route(name: 'app_activite_index', methods: ['GET', 'POST'])]
    public function index(Request $request, ActiviteRepository $activiteRepository): Response
    {
        // Create the search and filter form
        $form = $this->createForm(SearchActiviteType::class);
        $form->handleRequest($request);
    
        // Initialize search and filter parameters
        $searchTerm = '';
        $typeFilter = '';
    
        if ($form->isSubmitted() && $form->isValid()) {
            $formData = $form->getData();
            $searchTerm = $formData['search'] ?? '';
            $typeFilter = $formData['type'] ?? '';
        }
    
        // Get sorting parameters
        $sort = $request->query->get('sort', 'date');
        $direction = $request->query->get('direction', 'ASC');
    
        // Fetch filtered and sorted activities
        $activites = $activiteRepository->findBySearchAndFilter($searchTerm, $typeFilter, $sort, $direction);
    
        return $this->render('activite/index.html.twig', [
            'activites' => $activites,
            'form' => $form->createView(),
            'sort' => $sort,
            'direction' => $direction,
        ]);
    }
    

    #[Route('/listactivite',name: 'app_activite_back', methods: ['GET', 'POST'])]
    public function listActivitesBackend(Request $request, ActiviteRepository $activiteRepository): Response
    {
        $form = $this->createForm(SearchActiviteType::class);
        $form->handleRequest($request);
    
        // Initialize search and filter parameters
        $searchTerm = '';
        $typeFilter = '';
    
        if ($form->isSubmitted() && $form->isValid()) {
            $formData = $form->getData();
            $searchTerm = $formData['search'] ?? '';
            $typeFilter = $formData['type'] ?? '';
        }
    
        // Get sorting parameters
        $sort = $request->query->get('sort', 'date');
        $direction = $request->query->get('direction', 'ASC');
    
        // Fetch filtered and sorted activities
        $activites = $activiteRepository->findBySearchAndFilter($searchTerm, $typeFilter, $sort, $direction);
    
        return $this->render('activite/listActivitesBackend.html.twig', [
            'activites' => $activites,
            'form' => $form->createView(),
            'sort' => $sort,
            'direction' => $direction,
        ]);
    }

    

    #[Route('/new', name: 'app_activite_new', methods: ['GET', 'POST'])]
    public function new(Request $request, EntityManagerInterface $entityManager): Response
{
    $activite = new Activite();
    $form = $this->createForm(ActiviteType::class, $activite);
    $form->handleRequest($request);

    if ($form->isSubmitted()) {
        if ($form->isValid()) {
            $entityManager->persist($activite);
            $entityManager->flush();

            return $this->redirectToRoute('app_activite_index', [], Response::HTTP_SEE_OTHER);
        }
    }

    return $this->render('activite/new.html.twig', [
        'activite' => $activite,
        'form' => $form,
    ]);
}
    

    #[Route('/{id}', name: 'app_activite_show', methods: ['GET'])]
    public function show(Activite $activite): Response
    {
        return $this->render('activite/show.html.twig', [
            'activite' => $activite,
        ]);
    }

    #[Route('listactivite/{id}', name: 'app_activite_show_back', methods: ['GET'])]
    public function showBack(Activite $activite): Response
    {
        return $this->render('activite/showBack.html.twig', [
            'activite' => $activite,
        ]);
    }

    #[Route('/{id}/edit', name: 'app_activite_edit', methods: ['GET', 'POST'])]
    public function edit(Request $request, Activite $activite, EntityManagerInterface $entityManager): Response
    {
        $form = $this->createForm(ActiviteType::class, $activite);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $entityManager->flush();

            return $this->redirectToRoute('app_activite_index', [], Response::HTTP_SEE_OTHER);
        }

        return $this->render('activite/edit.html.twig', [
            'activite' => $activite,
            'form' => $form,
        ]);
    }

    #[Route('/{id}', name: 'app_activite_delete', methods: ['POST'])]
    public function delete(Request $request, Activite $activite, EntityManagerInterface $entityManager): Response
    {
        if ($this->isCsrfTokenValid('delete'.$activite->getId(), $request->getPayload()->getString('_token'))) {
            $entityManager->remove($activite);
            $entityManager->flush();
        }

        return $this->redirectToRoute('app_activite_index', [], Response::HTTP_SEE_OTHER);
    }


    #[Route('listactivite/{id}', name: 'app_activite_delete_back', methods: ['POST'])]
    public function deleteBack(Request $request, Activite $activite, EntityManagerInterface $entityManager): Response
    {
        if ($this->isCsrfTokenValid('delete'.$activite->getId(), $request->getPayload()->getString('_token'))) {
            $entityManager->remove($activite);
            $entityManager->flush();
        }

        return $this->redirectToRoute('app_activite_back', [], Response::HTTP_SEE_OTHER);
    }
}
