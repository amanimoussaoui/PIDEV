<?php

namespace App\Controller;

use App\Entity\Activite;
use App\Entity\Culture;
use App\Form\ActiviteType;
use App\Form\SearchActiviteType;
use App\Repository\ActiviteRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use Knp\Component\Pager\PaginatorInterface;


#[Route('/activite')]
final class ActiviteController extends AbstractController
{
    #[Route(name: 'app_activite_index', methods: ['GET'])]
    public function index(ActiviteRepository $activiteRepository): Response
    {
        $user = $this->getUser();
    
        $activites = $activiteRepository->findByUser($user);
    
        return $this->render('activite/index.html.twig', [
            'activites' => $activites,
        ]);
    }
    

    #[Route('/listactivite',name: 'app_activite_back', methods: ['GET', 'POST'])]
    public function listActivitesBackend(Request $request, ActiviteRepository $activiteRepository, PaginatorInterface $paginator): Response
    {
        $form = $this->createForm(SearchActiviteType::class);
        $form->handleRequest($request);
    
        $searchTerm = '';
        $typeFilter = '';
    
        if ($form->isSubmitted() && $form->isValid()) {
            $formData = $form->getData();
            $searchTerm = $formData['search'] ?? '';
            $typeFilter = $formData['type'] ?? '';
        }
    
        $sort = $request->query->get('sort', 'date');
        $direction = $request->query->get('direction', 'ASC');
    
        $query = $activiteRepository->findBySearchAndFilterBack($searchTerm, $typeFilter, $sort, $direction);
    
   // Paginer les résultats
   $page = $request->query->getInt('page', 1);
   $activites = $paginator->paginate(
       $query,
       $page,
       10 // Nombre d'éléments par page
   );

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

    $user = $this->getUser();

    // Retrieve cultureId from query parameters
    $cultureId = $request->query->get('cultureId');
    if ($cultureId) {
        $culture = $entityManager->getRepository(Culture::class)->find($cultureId);
        if ($culture) {
            $activite->setCulture($culture); // Pre-set the culture for the new activity
        }
    }

    // Retrieve date from query parameters
    $date = $request->query->get('date');
    if ($date) {
        try {
            $activite->setDate(new \DateTime($date));
        } catch (\Exception $e) {
            $activite->setDate(new \DateTime());
        }
    }

    $form = $this->createForm(ActiviteType::class, $activite, [
        'user' => $user,
    ]);

    $form->handleRequest($request);

    if ($form->isSubmitted() && $form->isValid()) {
        $entityManager->persist($activite);
        $entityManager->flush();

        return $this->redirectToRoute('app_activite_index', [], Response::HTTP_SEE_OTHER);
    }

    return $this->render('activite/new.html.twig', [
        'activite' => $activite,
        'form' => $form->createView(),
    ]);
}


    #[Route('/{id}', name: 'app_activite_show', methods: ['GET'])]
    public function show(Activite $activite): Response
    {
        return $this->render('activite/show.html.twig', [
            'activite' => $activite,
        ]);
    }

    #[Route('/listactivite/{id}', name: 'app_activite_show_back', methods: ['GET'])]
    public function showBack(Activite $activite): Response
    {
        return $this->render('activite/showBack.html.twig', [
            'activite' => $activite,
        ]);
    }

    #[Route('/{id}/edit', name: 'app_activite_edit', methods: ['GET', 'POST'])]
    public function edit(Request $request, Activite $activite, EntityManagerInterface $entityManager): Response
    {
        $user = $this->getUser();
    
        $form = $this->createForm(ActiviteType::class, $activite, [
            'user' => $user,
        ]);
    
        $form->handleRequest($request);
    
        if ($form->isSubmitted() && $form->isValid()) {
            $entityManager->flush();
    
            return $this->redirectToRoute('app_activite_index', [], Response::HTTP_SEE_OTHER);
        }
    
        return $this->render('activite/edit.html.twig', [
            'activite' => $activite,
            'form' => $form->createView(),
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


    #[Route('/listactivite/{id}', name: 'app_activite_delete_back', methods: ['POST'])]
    public function deleteBack(Request $request, Activite $activite, EntityManagerInterface $entityManager): Response
    {
        if ($this->isCsrfTokenValid('delete'.$activite->getId(), $request->getPayload()->getString('_token'))) {
            $entityManager->remove($activite);
            $entityManager->flush();
        }

        return $this->redirectToRoute('app_activite_back', [], Response::HTTP_SEE_OTHER);
    }
}
