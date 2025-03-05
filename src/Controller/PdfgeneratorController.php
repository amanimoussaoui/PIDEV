<?php

namespace App\Controller;

use App\Service\PdfGeneratorService;
use App\Repository\UtilisateursRepository;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;


final class PdfgeneratorController extends AbstractController{
    #[Route('/pdfgenerator', name: 'app_pdfgenerator')]
    public function index(): Response
    {
        return $this->render('pdfgenerator/index.html.twig', [
            'controller_name' => 'PdfgeneratorController',
        ]);
    }
    ////////////////////////////////////////////////////////////////
    #[Route('/pdfgenerator', name: 'app_pdfgenerator')]
    public function generatepdf(PdfGeneratorService $pdfGeneratorService , UtilisateursRepository $utilisateursRepository): Response
    {
        // Récupérer tous les utilisateurs
        $users = $utilisateursRepository->findAll();

        // Rendre la vue en HTML avec les utilisateurs
        $html = $this->renderView('pdfgenerator/pdf.html.twig', [
            'tabusers' => $users,
        ]);

        // Générer et retourner le PDF
        return $pdfGeneratorService->getStreamResponse($html, 'utilisateurs.pdf');
    }
}
