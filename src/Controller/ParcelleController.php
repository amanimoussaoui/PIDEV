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
use App\Repository\TerrainRepository;
use App\Service\MeteoService;
use Symfony\Component\HttpFoundation\JsonResponse;
use Knp\Component\Pager\PaginatorInterface;
use function str_replace;
use function base64_decode;
use function file_exists;
use function mkdir;
use function file_put_contents;
use function json_decode;


#[Route('/parcelle')]
final class ParcelleController extends AbstractController
{
    #[Route('/', name: 'app_parcelle_index', methods: ['GET', 'POST'])]
    public function index(ParcelleRepository $parcelleRepository, Request $request, TerrainRepository $terrainRepository,PaginatorInterface $paginator): Response
    {
        $user = $this->getUser();
        $hasTerrain = $terrainRepository->count(['utilisateur' => $user]) > 0;
        $form = $this->createForm(SearchParcelleType::class);
        $form->handleRequest($request);
        $searchCriteria = [];
        if ($form->isSubmitted() && $form->isValid()) {
            $searchCriteria = $form->getData();
        }
        $sort = $request->query->get('sort', 'superficie');
        $direction = $request->query->get('direction', 'ASC');
        $query = $parcelleRepository->findBySearchCriteria($searchCriteria, $sort, $direction, $user);

          // Paginate the results
          $parcelles = $paginator->paginate(
            $query, // Query to paginate
            $request->query->getInt('page', 1), // Page number, default to 1
            3 // Items per page
        );

        return $this->render('parcelle/index.html.twig', [
            'parcelles' => $parcelles,
            'form' => $form->createView(),
            'sort' => $sort,
            'direction' => $direction,
            'hasTerrain' => $hasTerrain,
        ]);
    }


    #[Route('/listparcelles', name: 'app_parcelle_back', methods: ['GET', 'POST'])]
    public function listParcellesBackend(
        ParcelleRepository $parcelleRepository,
        Request $request,
        PaginatorInterface $paginator
    ): Response {
        $form = $this->createForm(SearchParcelleType::class);
        $form->handleRequest($request);
    
        $searchCriteria = [];
        if ($form->isSubmitted() && $form->isValid()) {
            $searchCriteria = $form->getData();
        }
    
        $sort = $request->query->get('sort', 'superficie');
        $direction = $request->query->get('direction', 'ASC');
    
        // Get the query from the repository
        $query = $parcelleRepository->findBySearchCriteriaQuery($searchCriteria, $sort, $direction);
    
        // Paginate the results
        $parcelles = $paginator->paginate(
            $query, // Query to paginate
            $request->query->getInt('page', 1), // Page number, default to 1
            10 // Items per page
        );
    
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
    $user = $this->getUser(); // Get the logged-in user

    $parcelle->setUtilisateur($user);

    // Pass the user to the form
    $form = $this->createForm(ParcelleType::class, $parcelle, [
        'user' => $user,
    ]);
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
    public function show(Parcelle $parcelle, MeteoService $meteoService): Response
    {
        if ($parcelle->getLatitude() == null) {


            return $this->render('parcelle/show.html.twig', [
                'parcelle' => $parcelle,
                'weather' => null,

            ]);
        } else {
            $weatherData = $meteoService->getWeatherData($parcelle->getLatitude(), $parcelle->getLongitude());

            return $this->render('parcelle/show.html.twig', [
                'parcelle' => $parcelle,
                'weather' => $weatherData,
            ]);
        }
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
        $user = $this->getUser(); // Get the logged-in user
    
        // Pass the user to the form
        $form = $this->createForm(ParcelleType::class, $parcelle, [
            'user' => $user,
        ]);
        $form->handleRequest($request);
    
        if ($form->isSubmitted() && $form->isValid()) {
            $entityManager->flush();
    
            $this->addFlash('success', 'La parcelle a été modifiée avec succès.');
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

    #[Route('/{id}/save-coordinates', name: 'app_parcelle_save_coordinates', methods: ['POST'])]
    public function saveCoordinates(Request $request, Parcelle $parcelle, EntityManagerInterface $entityManager): Response
    {
        $imageData = $request->request->get('map_image');
        if ($imageData) {
            $imageData = str_replace('data:image/png;base64,', '', $imageData);
            $imageData = str_replace(' ', '+', $imageData);
            $imageBinary = base64_decode($imageData);

            // Define the upload directory
            $uploadDir = $this->getParameter('kernel.project_dir') . '/public/uploads/parcelles/';

            // Check if the directory exists, if not, create it
            if (!file_exists($uploadDir)) {
                mkdir($uploadDir, 0777, true); // Create the directory with proper permissions
            }

            $imageName = 'parcelle_' . $parcelle->getId() . '.png';
            $imagePath = $uploadDir . $imageName;

            // Save the image file
            file_put_contents($imagePath, $imageBinary);

            // Optionally, save the image path to the Parcelle entity
            $parcelle->setMapImage('/uploads/parcelles/' . $imageName);
            $entityManager->flush();
        }

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
    #[Route('/{id}/details', name: 'app_parcelle_details', methods: ['GET'])]
    public function getParcelleDetails(int $id, ParcelleRepository $parcelleRepository): JsonResponse
    {
        $parcelle = $parcelleRepository->find($id);

        if (!$parcelle) {
            return new JsonResponse([
                'success' => false,
                'error' => 'Parcelle not found.',
            ], 404);
        }

        return new JsonResponse([
            'success' => true,
            'parcelle' => [
                'superficie' => $parcelle->getSuperficie(),
                'typeSol' => $parcelle->getTypeSol(),
                'latitude' => $parcelle->getLatitude(),
                'longitude' => $parcelle->getLongitude(),
            ],
        ]);
    }
}
