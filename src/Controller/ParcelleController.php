<?php

namespace App\Controller;

use App\Entity\Parcelle;
use App\Form\ParcelleType;
use App\Form\SearchParcelleType;
use App\Repository\ParcelleRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use Knp\Component\Pager\PaginatorInterface;
use Doctrine\DBAL\Exception\ForeignKeyConstraintViolationException;


#[Route('/parcelle')]
final class ParcelleController extends AbstractController
{
    #[Route('/', name: 'app_parcelle_index', methods: ['GET', 'POST'])]
    public function index(ParcelleRepository $parcelleRepository, Request $request): Response
    {
        // Créer le formulaire de recherche et de filtrage
        $form = $this->createForm(SearchParcelleType::class);
        $form->handleRequest($request);

        // Récupérer les critères de recherche et de filtrage
        $searchCriteria = [];
        if ($form->isSubmitted() && $form->isValid()) {
            $searchCriteria = $form->getData();
        }

        // Récupérer les paramètres de tri
        $sort = $request->query->get('sort', 'superficie'); // Par défaut : tri par superficie
        $direction = $request->query->get('direction', 'ASC'); // Par défaut : ordre croissant

        // Récupérer les parcelles filtrées et triées
        $parcelles = $parcelleRepository->findBySearchCriteria($searchCriteria, $sort, $direction);

        return $this->render('parcelle/index.html.twig', [
            'parcelles' => $parcelles,
            'form' => $form->createView(),
            'sort' => $sort,
            'direction' => $direction,
        ]);
    }
    #[Route('/listparcelles', name: 'app_parcelle_back', methods: ['GET', 'POST'])]
    public function listParcellesBackend(
        ParcelleRepository $parcelleRepository,
        Request $request,
        PaginatorInterface $paginator
    ): Response {
        // Créer le formulaire de recherche et de filtrage
        $form = $this->createForm(SearchParcelleType::class);
        $form->handleRequest($request);

        // Récupérer les critères de recherche et de filtrage
        $searchCriteria = [];
        if ($form->isSubmitted() && $form->isValid()) {
            $searchCriteria = $form->getData();
        }

        // Récupérer les paramètres de tri
        $sort = $request->query->get('sort', 'superficie'); // Par défaut : tri par superficie
        $direction = $request->query->get('direction', 'ASC'); // Par défaut : ordre croissant

        // Récupérer les parcelles filtrées et triées
        $parcelles = $parcelleRepository->findBySearchCriteria($searchCriteria, $sort, $direction);

        return $this->render('parcelle/listParcellesBackend.html.twig', [
            'parcelles' => $parcelles,
            'form' => $form->createView(),
            'sort' => $sort,
            'direction' => $direction,
        ]);
    }


    #[Route('/new', name: 'app_parcelle_new', methods: ['GET', 'POST'])]
    public function new(Request $request, EntityManagerInterface $entityManager): Response
    {
        $parcelle = new Parcelle();
        $form = $this->createForm(ParcelleType::class, $parcelle);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $entityManager->persist($parcelle);
            $entityManager->flush();

            $this->addFlash('success', 'La parcelle a été créée avec succès.');
            return $this->redirectToRoute('app_parcelle_index');
        } else {
            $this->addFlash('error', 'Il y a des erreurs dans le formulaire.');
        }

        return $this->render('parcelle/new.html.twig', [
            'parcelle' => $parcelle,
            'form' => $form,
        ]);
    }

    #[Route('/{id}', name: 'app_parcelle_show', methods: ['GET'])]
    public function show(Parcelle $parcelle): Response
    {
        return $this->render('parcelle/show.html.twig', [
            'parcelle' => $parcelle,
        ]);
    }

    #[Route('listparcelles/{id}', name: 'app_parcelle_show_back', methods: ['GET'])]
    public function showBack(Parcelle $parcelle): Response
    {
        return $this->render('parcelle/showBack.html.twig', [
            'parcelle' => $parcelle,
        ]);
    }

    #[Route('/{id}/edit', name: 'app_parcelle_edit', methods: ['GET', 'POST'])]
    public function edit(Request $request, Parcelle $parcelle, EntityManagerInterface $entityManager): Response
    {
        $form = $this->createForm(ParcelleType::class, $parcelle);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $entityManager->flush();

            return $this->redirectToRoute('app_parcelle_index', [], Response::HTTP_SEE_OTHER);
        }

        return $this->render('parcelle/edit.html.twig', [
            'parcelle' => $parcelle,
            'form' => $form,
        ]);
    }

    #[Route('/{id}', name: 'app_parcelle_delete', methods: ['POST'])]
    public function delete(Request $request, Parcelle $parcelle, EntityManagerInterface $entityManager): Response
    {
        if ($this->isCsrfTokenValid('delete' . $parcelle->getId(), $request->getPayload()->getString('_token'))) {
            $entityManager->remove($parcelle);
            $entityManager->flush();
        }

        return $this->redirectToRoute('app_parcelle_index', [], Response::HTTP_SEE_OTHER);
    }

    #[Route('listparcelles/{id}', name: 'app_parcelle_delete_back', methods: ['POST'])]
    public function deleteBack(Request $request, Parcelle $parcelle, EntityManagerInterface $entityManager): Response
    {
        if ($this->isCsrfTokenValid('delete' . $parcelle->getId(), $request->getPayload()->getString('_token'))) {
            $entityManager->remove($parcelle);
            $entityManager->flush();
        }
    
        return $this->redirectToRoute('app_parcelle_back', [], Response::HTTP_SEE_OTHER);
    }
}
