<?php

namespace App\Controller;

use App\Entity\Culture;
use App\Form\CultureType;
use App\Form\SearchCultureType;
use App\Repository\CultureRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

#[Route('/culture')]
final class CultureController extends AbstractController
{
    #[Route( name: 'app_culture_index', methods: ['GET', 'POST'])]
    public function index(Request $request, CultureRepository $cultureRepository): Response
    {
        // Create the form
        $form = $this->createForm(SearchCultureType::class);
        $form->handleRequest($request);
    
        // Initialize search and filter parameters
        $searchTerm = '';
        $statutFilter = '';
    
        // Check if the form is submitted and valid
        if ($form->isSubmitted() && $form->isValid()) {
            $formData = $form->getData();
            $searchTerm = $formData['search'] ?? '';
            $statutFilter = $formData['statut'] ?? '';
        }
    
        // Get sorting parameters
        $sort = $request->query->get('sort', 'id');
        $direction = $request->query->get('direction', 'ASC');
    
        // Fetch filtered and sorted cultures
        $cultures = $cultureRepository->findBySearchAndFilter($searchTerm, $statutFilter, $sort, $direction);
    
        return $this->render('culture/index.html.twig', [
            'cultures' => $cultures,
            'form' => $form->createView(),
            'sort' => $sort,
            'direction' => $direction,
        ]);
    }

    #[Route('/listcultures', name: 'app_culture_back', methods: ['GET', 'POST'])]
    public function listCulturesBackend(Request $request, CultureRepository $cultureRepository): Response
    {
        // Create and handle the form
        $form = $this->createForm(SearchCultureType::class);
        $form->handleRequest($request);
    
        // Extract search/filter criteria
        $searchTerm = $form->get('search')->getData() ?? '';
        $statutFilter = $form->get('statut')->getData() ?? '';
    
        // Get sorting parameters
        $sort = $request->query->get('sort', 'id');
        $direction = $request->query->get('direction', 'ASC');
    
        // Fetch filtered & sorted cultures
        $cultures = $cultureRepository->findBySearchAndFilter($searchTerm, $statutFilter, $sort, $direction);
    
        return $this->render('culture/listCulturesBackend.html.twig', [
            'cultures' => $cultures,
            'form' => $form->createView(),
            'sort' => $sort,
            'direction' => $direction,
        ]);
    }
    

    #[Route('/new', name: 'app_culture_new', methods: ['GET', 'POST'])]
    public function new(Request $request, EntityManagerInterface $entityManager): Response
    {
        $culture = new Culture();
        $form = $this->createForm(CultureType::class, $culture);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $entityManager->persist($culture);
            $entityManager->flush();

            return $this->redirectToRoute('app_culture_index', [], Response::HTTP_SEE_OTHER);
        }

        return $this->render('culture/new.html.twig', [
            'culture' => $culture,
            'form' => $form,
        ]);
    }

  

    #[Route('/{id}', name: 'app_culture_show', methods: ['GET'])]
    public function show(Culture $culture): Response
    {
        return $this->render('culture/show.html.twig', [
            'culture' => $culture,
        ]);
    }

    #[Route('listparcelles/{id}', name: 'app_culture_show_back', methods: ['GET'])]
    public function showBack(Culture $culture): Response
    {
        return $this->render('culture/showBack.html.twig', [
            'culture' => $culture,
        ]);
    }

    #[Route('/{id}/edit', name: 'app_culture_edit', methods: ['GET', 'POST'])]
    public function edit(Request $request, Culture $culture, EntityManagerInterface $entityManager): Response
    {
        $form = $this->createForm(CultureType::class, $culture);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $entityManager->flush();

            return $this->redirectToRoute('app_culture_index', [], Response::HTTP_SEE_OTHER);
        }

        return $this->render('culture/edit.html.twig', [
            'culture' => $culture,
            'form' => $form,
        ]);
    }

    #[Route('/{id}', name: 'app_culture_delete', methods: ['POST'])]
    public function delete(Request $request, Culture $culture, EntityManagerInterface $entityManager): Response
    {
        if ($this->isCsrfTokenValid('delete'.$culture->getId(), $request->getPayload()->getString('_token'))) {
            $entityManager->remove($culture);
            $entityManager->flush();
        }

        return $this->redirectToRoute('app_culture_index', [], Response::HTTP_SEE_OTHER);
    }

    #[Route('listparcelles/{id}', name: 'app_culture_delete_back', methods: ['POST'])]
    public function deleteBack(Request $request, Culture $culture, EntityManagerInterface $entityManager): Response
    {
        if ($this->isCsrfTokenValid('delete'.$culture->getId(), $request->getPayload()->getString('_token'))) {
            $entityManager->remove($culture);
            $entityManager->flush();
        }

        return $this->redirectToRoute('app_culture_back', [], Response::HTTP_SEE_OTHER);
    }
}
