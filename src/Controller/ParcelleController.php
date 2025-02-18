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
        // Get the currently logged-in user
        $user = $this->getUser();
    
        // Create the search form
        $form = $this->createForm(SearchParcelleType::class);
        $form->handleRequest($request);
    
        // Initialize search criteria
        $searchCriteria = [];
        if ($form->isSubmitted() && $form->isValid()) {
            $searchCriteria = $form->getData();
        }
    
        // Get sorting parameters from the request
        $sort = $request->query->get('sort', 'superficie');
        $direction = $request->query->get('direction', 'ASC');
    
        // Fetch parcelles for the logged-in user
        $parcelles = $parcelleRepository->findBySearchCriteria($searchCriteria, $sort, $direction, $user);
    
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
    ): Response {
        
        $form = $this->createForm(SearchParcelleType::class);
        $form->handleRequest($request);

        
        $searchCriteria = [];
        if ($form->isSubmitted() && $form->isValid()) {
            $searchCriteria = $form->getData();
        }

        
        $sort = $request->query->get('sort', 'superficie'); 
        $direction = $request->query->get('direction', 'ASC'); 

        
        $parcelles = $parcelleRepository->findBySearchCriteriaQuery($searchCriteria, $sort, $direction);

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

    $user = $this->getUser();

    $parcelle->setUtilisateur($user);

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

    #[Route('/listparcelles/{id}', name: 'app_parcelle_show_back', methods: ['GET'])]
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

    #[Route('/listparcelles/{id}', name: 'app_parcelle_delete_back', methods: ['POST'])]
    public function deleteBack(Request $request, Parcelle $parcelle, EntityManagerInterface $entityManager): Response
    {
        if ($this->isCsrfTokenValid('delete' . $parcelle->getId(), $request->getPayload()->getString('_token'))) {
            $entityManager->remove($parcelle);
            $entityManager->flush();
        }
    
        return $this->redirectToRoute('app_parcelle_back', [], Response::HTTP_SEE_OTHER);
    }


    #[Route('/parcelle/{id}/save-coordinates', name: 'app_parcelle_save_coordinates', methods: ['POST'])]
public function saveCoordinates(Request $request, Parcelle $parcelle, EntityManagerInterface $entityManager): Response
{
    $latitude = $request->request->get('latitude');
    $longitude = $request->request->get('longitude');
    $boundary = json_decode($request->request->get('boundary'), true);

    $parcelle->setLatitude($latitude);
    $parcelle->setLongitude($longitude);
    $parcelle->setBoundary($boundary);

    $entityManager->flush();

    $this->addFlash('success', 'Les coordonnées de la parcelle ont été enregistrées avec succès.');
    return $this->redirectToRoute('app_parcelle_show', ['id' => $parcelle->getId()]);
}

}
