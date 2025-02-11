<?php

namespace App\Controller;

use App\Entity\Recolte;
use App\Form\RecolteType;
use App\Form\SearchRecolteType; 
use App\Repository\RecolteRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

#[Route('/recolte')]
final class RecolteController extends AbstractController
{
    #[Route(name: 'app_recolte_index', methods: ['GET', 'POST'])]
    public function index(Request $request, RecolteRepository $recolteRepository): Response
    {
        // Create the search form
        $form = $this->createForm(SearchRecolteType::class);
        $form->handleRequest($request);
    
        // Initialize search parameters
        $searchTerm = '';
        $qualiteFilter = '';
    
        // Check if the form is submitted and valid
        if ($form->isSubmitted() && $form->isValid()) {
            $formData = $form->getData();
            $searchTerm = $formData['search'] ?? '';
            $qualiteFilter = $formData['qualite'] ?? ''; // Get the quality filter
        }
    
        // Get sorting parameters
        $sort = $request->query->get('sort', 'dateRecolte'); // Default sort by dateRecolte
        $direction = $request->query->get('direction', 'ASC');
    
        // Fetch filtered and sorted recoltes
        $recoltes = $recolteRepository->findBySearchAndFilter($searchTerm, $qualiteFilter, $sort, $direction);
    
        return $this->render('recolte/index.html.twig', [
            'recoltes' => $recoltes,
            'form' => $form->createView(),
            'sort' => $sort,
            'direction' => $direction,
        ]);
    }
    
    #[Route('/listrecoltes', name: 'app_recolte_back', methods: ['GET', 'POST'])]
    public function listRecoltesBackend(Request $request, RecolteRepository $recolteRepository): Response
    {
        // Create and handle the search form
        $form = $this->createForm(SearchRecolteType::class);
        $form->handleRequest($request);
    
        // Initialize search parameters
        $searchTerm = '';
        $qualiteFilter = '';
    
        // Check if the form is submitted and valid
        if ($form->isSubmitted() && $form->isValid()) {
            $formData = $form->getData();
            $searchTerm = $formData['search'] ?? '';
            $qualiteFilter = $formData['qualite'] ?? ''; // Get the quality filter
        }
    
        // Get sorting parameters
        $sort = $request->query->get('sort', 'dateRecolte'); // Default sort by dateRecolte
        $direction = $request->query->get('direction', 'ASC');
    
        // Fetch filtered and sorted recoltes
        $recoltes = $recolteRepository->findBySearchAndFilter($searchTerm, $qualiteFilter, $sort, $direction);
    
        return $this->render('recolte/listRecolteBackend.html.twig', [
            'recoltes' => $recoltes,
            'form' => $form->createView(),
            'sort' => $sort,
            'direction' => $direction,
        ]);
    }

    #[Route('/new', name: 'app_recolte_new', methods: ['GET', 'POST'])]
    public function new(Request $request, EntityManagerInterface $entityManager): Response
    {
        $recolte = new Recolte();
        $form = $this->createForm(RecolteType::class, $recolte);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $entityManager->persist($recolte);
            $entityManager->flush();

            return $this->redirectToRoute('app_recolte_index', [], Response::HTTP_SEE_OTHER);
        }

        return $this->render('recolte/new.html.twig', [
            'recolte' => $recolte,
            'form' => $form,
        ]);
    }

    #[Route('listparcelles/{id}', name: 'app_recolte_show_back', methods: ['GET'])]
    public function showBack(Recolte $recolte): Response
    {
        return $this->render('recolte/showBack.html.twig', [
            'recolte' => $recolte,
        ]);
    }

    #[Route('/{id}', name: 'app_recolte_show', methods: ['GET'])]
    public function show(Recolte $recolte): Response
    {
        return $this->render('recolte/show.html.twig', [
            'recolte' => $recolte,
        ]);
    }

    #[Route('/{id}/edit', name: 'app_recolte_edit', methods: ['GET', 'POST'])]
    public function edit(Request $request, Recolte $recolte, EntityManagerInterface $entityManager): Response
    {
        $form = $this->createForm(RecolteType::class, $recolte);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $entityManager->flush();

            return $this->redirectToRoute('app_recolte_index', [], Response::HTTP_SEE_OTHER);
        }

        return $this->render('recolte/edit.html.twig', [
            'recolte' => $recolte,
            'form' => $form,
        ]);
    }

    #[Route('/{id}', name: 'app_recolte_delete', methods: ['POST'])]
    public function delete(Request $request, Recolte $recolte, EntityManagerInterface $entityManager): Response
    {
        if ($this->isCsrfTokenValid('delete'.$recolte->getId(), $request->request->get('_token'))) {
            $entityManager->remove($recolte);
            $entityManager->flush();
        }

        return $this->redirectToRoute('app_recolte_index', [], Response::HTTP_SEE_OTHER);
    }
   
    #[Route('listparcelles/{id}', name: 'app_recolte_delete_back', methods: ['POST'])]
    public function deleteBack(Request $request, Recolte $recolte, EntityManagerInterface $entityManager): Response
    {
        if ($this->isCsrfTokenValid('delete'.$recolte->getId(), $request->request->get('_token'))) {
            $entityManager->remove($recolte);
            $entityManager->flush();
        }

        return $this->redirectToRoute('app_recolte_back', [], Response::HTTP_SEE_OTHER);
    }
}