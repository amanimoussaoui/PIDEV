<?php

namespace App\Controller;

use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use App\Repository\ParcelleRepository;
use App\Repository\CultureRepository;
use App\Repository\RecolteRepository;
use App\Repository\ActiviteRepository;

final class RendementController extends AbstractController
{
    #[Route('/rendement', name: 'app_rendement')]
    public function index(
        ParcelleRepository $parcelleRepository,
        CultureRepository $cultureRepository,
        RecolteRepository $recolteRepository,
        ActiviteRepository $activiteRepository
    ): Response {
        // Get the logged-in user
        $user = $this->getUser();

        // Fetch data for the dashboard (filtered by user)
        $totalParcelles = $parcelleRepository->count(['utilisateur' => $user]);
        $totalCultures = $cultureRepository->countByUser($user);
        $totalRecoltes = $recolteRepository->countByUser($user);
        $totalActivites = $activiteRepository->countByUser($user);

        // New data for Récolte and Activités charts
        $totalRevenue = $recolteRepository->getTotalRevenueByUser($user);
        $recolteMonthsData = $recolteRepository->getRecolteMonthsByUser($user);
        $recolteQuantitiesData = $recolteRepository->getRecolteQuantitiesByUser($user);
        $recolteQualities = $recolteRepository->getRecolteQualityDistributionByUser($user);
        $activityTypes = $activiteRepository->getActivityTypeDistributionByUser($user);
        $activityCrops = $activiteRepository->getActivityCropDistributionByUser($user);

        // Fetch data for charts (filtered by user)
        $parcelleSoilTypes = $parcelleRepository->getSoilTypeDistributionByUser($user);
        $cropTypes = $cultureRepository->getCropTypeDistributionByUser($user);
        $cropStatuses = $cultureRepository->getCropStatusDistributionByUser($user);

        // New data for additional charts
        $parcelleYieldMonths = $parcelleRepository->getParcelleYieldMonthsByUser($user);
        $parcelleYieldData = $parcelleRepository->getParcelleYieldDataByUser($user);
        $cultureTypes = $cultureRepository->getCultureTypesByUser($user);

        $cultureYields = $cultureRepository->getCultureYieldsByUser($user);
        $recolteCultureTypes = $recolteRepository->getRecolteCultureTypesByUser($user);
        $recolteCultureQuantities = $recolteRepository->getRecolteCultureQuantitiesByUser($user);
        $activityMonths = $activiteRepository->getActivityMonthsByUser($user);
        $activityFrequencies = $activiteRepository->getActivityFrequenciesByUser($user);
        $activityCultures = $activiteRepository->getActivityCulturesByUser($user);
        $activityCounts = $activiteRepository->getActivityCountsByUser($user);

        // Process data for charts
        $recolteMonths = array_column($recolteMonthsData, 'month');
        $recolteQuantities = array_column($recolteQuantitiesData, 'quantity');

        return $this->render('rendement/index.html.twig', [
            'totalParcelles' => $totalParcelles,
            'totalCultures' => $totalCultures,
            'totalRecoltes' => $totalRecoltes,
            'totalActivites' => $totalActivites,
            'parcelleSoilTypes' => $parcelleSoilTypes,
            'cropTypes' => $cropTypes,
            'cropStatuses' => $cropStatuses,
            'totalRevenue' => $totalRevenue,
            'recolteMonths' => $recolteMonths,
            'recolteQuantities' => $recolteQuantities,
            'recolteQualities' => $recolteQualities,
            'activityTypes' => $activityTypes,
            'activityCrops' => $activityCrops,
            'parcelleYieldMonths' => $parcelleYieldMonths,
            'parcelleYieldData' => $parcelleYieldData,
            'cultureTypes' => $cultureTypes,
            'cultureYields' => $cultureYields,
            'recolteCultureTypes' => $recolteCultureTypes,
            'recolteCultureQuantities' => $recolteCultureQuantities,
            'activityMonths' => $activityMonths,
            'activityFrequencies' => $activityFrequencies,
            'activityCultures' => $activityCultures,
            'activityCounts' => $activityCounts,
        ]);
    }
}