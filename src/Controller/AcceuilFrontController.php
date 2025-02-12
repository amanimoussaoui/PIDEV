<?php

namespace App\Controller;

use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\HttpFoundation\Request;

final class AcceuilFrontController extends AbstractController
{
    #[Route('/acceuil', name: 'app_acceuil')]
    public function acceuilPage(): Response
    {
        return $this->render('acceuil_front/acceuil.html.twig', [
            'controller_name' => 'AcceuilFrontController',
        ]);
    }

    #[Route('/about', name: 'app_about')]
    public function aboutPage(): Response
    {
        return $this->render('acceuil_front/about.html.twig', [
            'controller_name' => 'AcceuilFrontController',
        ]);
    }

    #[Route('/services', name: 'app_services')]
    public function servicesPage(): Response
    {
        return $this->render('acceuil_front/services.html.twig', [
            'controller_name' => 'AcceuilFrontController',
        ]);
    }
}