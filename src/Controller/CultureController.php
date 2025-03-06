<?php

namespace App\Controller;

use App\Entity\Culture;
use App\Entity\Activite;
use App\Entity\Recolte;
use App\Form\CultureType;
use App\Form\SearchCultureType;
use App\Repository\CultureRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\HttpFoundation\JsonResponse;
use Psr\Log\LoggerInterface;
use Knp\Component\Pager\PaginatorInterface;


#[Route('/culture')]
final class CultureController extends AbstractController
{
    #[Route(name: 'app_culture_index', methods: ['GET', 'POST'])]
    public function index(Request $request, CultureRepository $cultureRepository, PaginatorInterface $paginator): Response
    {
        $user = $this->getUser();
    
        $form = $this->createForm(SearchCultureType::class);
        $form->handleRequest($request);
    
        $searchTerm = '';
        $statutFilter = '';
    
        if ($form->isSubmitted() && $form->isValid()) {
            $formData = $form->getData();
            $searchTerm = $formData['search'] ?? '';
            $statutFilter = $formData['statut'] ?? '';
        }
    
        // Get sorting parameters from the request
        $sort = $request->query->get('sort', 'id');
        $direction = $request->query->get('direction', 'ASC');
    
        // Fetch cultures for the logged-in user
        $query = $cultureRepository->findBySearchAndFilter($searchTerm, $statutFilter, $sort, $direction, $user);
    
        
   // Paginer les résultats
   $page = $request->query->getInt('page', 1);
   $cultures = $paginator->paginate(
       $query,
       $page,
       6 // Nombre d'éléments par page
   );
        return $this->render('culture/index.html.twig', [
            'cultures' => $cultures,
            'form' => $form->createView(),
            'sort' => $sort,
            'direction' => $direction,
        ]);
    }

    
    #[Route('/listcultures', name: 'app_culture_back', methods: ['GET', 'POST'])]
    public function listCulturesBackend(Request $request, CultureRepository $cultureRepository, PaginatorInterface $paginator): Response
    {

        $form = $this->createForm(SearchCultureType::class);
        $form->handleRequest($request);


        $searchTerm = $form->get('search')->getData() ?? '';
        $statutFilter = $form->get('statut')->getData() ?? '';


        $sort = $request->query->get('sort', 'id');
        $direction = $request->query->get('direction', 'ASC');


        $query = $cultureRepository->findBySearchAndFilterBack($searchTerm, $statutFilter, $sort, $direction);


   // Paginer les résultats
   $page = $request->query->getInt('page', 1);
   $cultures = $paginator->paginate(
       $query,
       $page,
       10 // Nombre d'éléments par page
   );

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

    $user = $this->getUser();

    $form = $this->createForm(CultureType::class, $culture, [
        'user' => $user,
    ]);

    //$culture->setStatut('en_culture');

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
    public function show(Culture $culture, EntityManagerInterface $entityManager): Response
    {
        $activities = $entityManager->getRepository(Activite::class)->findBy(['culture' => $culture]);

        $recolte = $entityManager->getRepository(Recolte::class)->findOneBy(['culture' => $culture]);

        return $this->render('culture/show.html.twig', [
            'culture' => $culture,
            'activities' => $activities,
            'recolte' => $recolte,
        ]);
    }

    #[Route('/listcultures/{id}', name: 'app_culture_show_back', methods: ['GET'])]
    public function showBack(Culture $culture): Response
    {
        return $this->render('culture/showBack.html.twig', [
            'culture' => $culture,
        ]);
    }

    #[Route('/{id}/edit', name: 'app_culture_edit', methods: ['GET', 'POST'])]
    public function edit(Request $request, Culture $culture, EntityManagerInterface $entityManager): Response
    {
        $user = $this->getUser();
    
        $form = $this->createForm(CultureType::class, $culture, [
            'user' => $user,
        ]);
    
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
        if ($this->isCsrfTokenValid('delete' . $culture->getId(), $request->getPayload()->getString('_token'))) {
            $entityManager->remove($culture);
            $entityManager->flush();
        }

        return $this->redirectToRoute('app_culture_index', [], Response::HTTP_SEE_OTHER);
    }

    #[Route('/listparcelles/{id}', name: 'app_culture_delete_back', methods: ['POST'])]
    public function deleteBack(Request $request, Culture $culture, EntityManagerInterface $entityManager): Response
    {
        if ($this->isCsrfTokenValid('delete' . $culture->getId(), $request->getPayload()->getString('_token'))) {
            $entityManager->remove($culture);
            $entityManager->flush();
        }

        return $this->redirectToRoute('app_culture_back', [], Response::HTTP_SEE_OTHER);
    }

    #[Route('/{id}/update-status', name: 'app_culture_update_status', methods: ['POST'])]
    public function updateStatus(Request $request, Culture $culture, EntityManagerInterface $entityManager): Response
    {
        $culture->setStatut('terminé');
        $entityManager->flush();

        return $this->redirectToRoute('app_recolte_new', [
            'culture_id' => $culture->getId(),
        ], Response::HTTP_SEE_OTHER);
    }


    #[Route('/culture/predict', name: 'app_culture_predict', methods: ['POST'])]
    public function predictYield(Request $request, LoggerInterface $logger): JsonResponse
    {
        try {
            // Log the incoming request data
            $logger->info('Received prediction request:', ['data' => $request->getContent()]);
    
            // Get JSON data from the request
            $formData = json_decode($request->getContent(), true);
    
            if ($formData === null) {
                throw new \Exception('Invalid JSON data in request.');
            }
    
            // Log the decoded form data
            $logger->info('Decoded form data:', ['formData' => $formData]);
    
            // Validate form data
            if (empty($formData['culture[nomCulture]']) || empty($formData['culture[dateSemis]']) || empty($formData['culture[duree]']) || empty($formData['culture[statut]']) || empty($formData['parcelle'])) {
                throw new \Exception('Missing or invalid form data.');
            }
    
            // Ensure parcelle data is correctly structured
            if (!isset($formData['parcelle']['superficie']) || !isset($formData['parcelle']['typeSol']) || !isset($formData['parcelle']['latitude']) || !isset($formData['parcelle']['longitude'])) {
                throw new \Exception('Missing or invalid parcelle data.');
            }
    
            // Extract and preprocess the form data
            $inputData = [
                'culture_id' => 0, // Placeholder, as this is a new culture
                'crop_type_Orge' => ($formData['culture[nomCulture]'] === 'Orge') ? 1 : 0,
                'crop_type_Blé' => ($formData['culture[nomCulture]'] === 'Blé') ? 1 : 0,
                'crop_type_Maïs' => ($formData['culture[nomCulture]'] === 'Maïs') ? 1 : 0,
                'sowing_year' => (int)date('Y', strtotime($formData['culture[dateSemis]'])),
                'sowing_month' => (int)date('m', strtotime($formData['culture[dateSemis]'])),
                'sowing_day' => (int)date('d', strtotime($formData['culture[dateSemis]'])),
                'harvest_year' => (int)date('Y', strtotime($formData['culture[dateSemis]'] . " +{$formData['culture[duree]']} days")),
                'harvest_month' => (int)date('m', strtotime($formData['culture[dateSemis]'] . " +{$formData['culture[duree]']} days")),
                'harvest_day' => (int)date('d', strtotime($formData['culture[dateSemis]'] . " +{$formData['culture[duree]']} days")),
                'growth_duration' => (int)$formData['culture[duree]'],
                'area' => (float)$formData['parcelle']['superficie'], // Extract superficie from parcelle
                'soil_type_argileux' => ($formData['parcelle']['typeSol'] === 'argileux') ? 1 : 0,
                'soil_type_sableux' => ($formData['parcelle']['typeSol'] === 'sableux') ? 1 : 0,
                'latitude' => (float)$formData['parcelle']['latitude'],
                'longitude' => (float)$formData['parcelle']['longitude'],
                'yield_quality_Bonne' => 0, // Placeholder
                'yield_quality_Excellente' => 0, // Placeholder
                'yield_quality_Moyenne' => 1, // Placeholder
                'status_en_culture' => ($formData['culture[statut]'] === 'en_culture') ? 1 : 0,
                'status_terminé' => ($formData['culture[statut]'] === 'terminé') ? 1 : 0,
            ];
    
            // Log the preprocessed input data
            $logger->info('Preprocessed input data:', ['inputData' => $inputData]);
    
            // Call the Python API (Flask or FastAPI)
            $prediction = $this->callPythonApi($inputData);
    
            if ($prediction !== null) {
                return new JsonResponse([
                    'success' => true,
                    'prediction' => $prediction,
                ]);
            } else {
                return new JsonResponse([
                    'success' => false,
                    'error' => 'Failed to make prediction.',
                ]);
            }
        } catch (\Exception $e) {
            // Log the exception
            $logger->error('Error in predictYield:', ['error' => $e->getMessage()]);
    
            // Return a JSON response with the error message
            return new JsonResponse([
                'success' => false,
                'error' => $e->getMessage(),
            ], 500);
        }
    }
    
    private function callPythonApi(array $inputData): ?float
    {
        // Call the Python API (Flask)
        $apiUrl = 'http://localhost:5000/predict'; // URL of the Python API
        $ch = curl_init($apiUrl);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        curl_setopt($ch, CURLOPT_POST, true);
        curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($inputData));
        curl_setopt($ch, CURLOPT_HTTPHEADER, [
            'Content-Type: application/json',
        ]);
    
        $response = curl_exec($ch);
        curl_close($ch);
    
        if ($response) {
            $responseData = json_decode($response, true);
            return $responseData['predicted_yield'] ?? null;
        }
    
        return null;
    }
}