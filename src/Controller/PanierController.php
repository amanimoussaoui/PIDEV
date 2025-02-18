<?php

namespace App\Controller;

use App\Entity\Panier;
use App\Entity\Product;
use App\Repository\PanierRepository;
use App\Repository\CommandeRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;
use Dompdf\Dompdf;
use Dompdf\Options;

#[Route('/panier')]
class PanierController extends AbstractController
{
    /**
     * Afficher tous les produits du panier
     */
    #[Route('/', name: 'panier_index', methods: ['GET'])]
    public function index(PanierRepository $panierRepository): Response
    {
        $paniers = $panierRepository->findAll();
        $totalGeneral = array_reduce($paniers, function ($total, $panier) {
            return $total + $panier->getTotale();
        }, 0);
    
        return $this->render('panier/index.html.twig', [
            'paniers' => $paniers,
            'totalGeneral' => $totalGeneral
        ]);
    }
    /**
     * Ajouter un produit au panier
     */
    #[Route('/ajouter/{id}', name: 'panier_ajouter', methods: ['GET', 'POST'])]
    public function ajouter(Product $product, EntityManagerInterface $entityManager): Response
    {
        $panier = $entityManager->getRepository(Panier::class)->findOneBy(['product' => $product]);

        if ($panier) {
            $panier->setQuantite($panier->getQuantite() + 1);
            $panier->setTotale($panier->getQuantite() * $product->getPrix());
        } else {
            $panier = new Panier();
            $panier->setProduct($product);
            $panier->setQuantite(1);
            $panier->setTotale($product->getPrix());
            $entityManager->persist($panier);
        }

        $entityManager->flush();
        $this->addFlash('success', 'Produit ajouté au panier !');

        return $this->redirectToRoute('panier_index');
    }

    /**
     * Modifier la quantité d'un produit dans le panier
     */
    #[Route('/modifier/{id}', name: 'panier_modifier', methods: ['POST'])]
    public function modifierQuantite(Request $request, Panier $panier, EntityManagerInterface $entityManager): Response
    {
        $quantite = $request->request->get('quantite');

        if ($quantite > 0) {
            $panier->setQuantite($quantite);
            $panier->setTotale($quantite * $panier->getProduct()->getPrix());
            $entityManager->flush();
        }

        return $this->redirectToRoute('panier_index');
    }

    /**
     * Supprimer un produit du panier
     */
    #[Route('/supprimer/{id}', name: 'panier_supprimer', methods: ['POST'])]
    public function supprimer(Panier $panier, EntityManagerInterface $entityManager): Response
    {
        $entityManager->remove($panier);
        $entityManager->flush();
        $this->addFlash('success', 'Produit supprimé du panier !');

        return $this->redirectToRoute('panier_index');
    }

    /**
     * Vider complètement le panier
     */
    #[Route('/vider', name: 'panier_vider', methods: ['POST'])]
    public function vider(EntityManagerInterface $entityManager, PanierRepository $panierRepository): Response
    {
        $paniers = $panierRepository->findAll();

        foreach ($paniers as $panier) {
            $entityManager->remove($panier);
        }

        $entityManager->flush();
        $this->addFlash('success', 'Panier vidé avec succès !');

        return $this->redirectToRoute('panier_index');
    }
    #[Route('/admin', name: 'panier_admin_index', methods: ['GET'])]
    public function adminIndex(PanierRepository $panierRepository): Response
    {
        $paniers = $panierRepository->findAll();

$totalGeneral = array_reduce($paniers, function ($total, $panier) {
            return $total + $panier->getTotale();
        }, 0);

        return $this->render('panier/admin_index.html.twig', [
            'paniers' => $paniers,
            'totalGeneral' => $totalGeneral,
        ]);
    }

    /**
    
     * Supprimer un produit du panier (Admin)
     */
    #[Route('/admin/supprimer/{id}', name: 'panier_admin_supprimer', methods: ['POST'])]
    public function adminSupprimer(Panier $panier, EntityManagerInterface $entityManager): Response
    {
        $entityManager->remove($panier);
        $entityManager->flush();

        $this->addFlash('success', 'Produit supprimé du panier avec succès !');

        return $this->redirectToRoute('panier_admin_index');
    }
    /**
 * Afficher les détails de la commande
 */
/*#[Route('/commande/details/{id}', name: 'panier_commande_details', methods: ['GET'])]
public function details(int $id, CommandeRepository $commandeRepository): Response
{
    // Récupérer la commande à partir de son ID
    $commande = $commandeRepository->find($id);

    if (!$commande) {
        throw $this->createNotFoundException('Commande non trouvée.');
    }

    // Calculer le total général des produits dans la commande
    $totalGeneral = 0;
    foreach ($commande->getPaniers() as $panier) {
        $totalGeneral += $panier->getTotale(); // Assurez-vous que la méthode getTotale() existe dans Panier
    }

    // Rendre la vue avec les détails de la commande
    return $this->render('panier/commande_details.html.twig', [
        'commande' => $commande,
        'totalGeneral' => $totalGeneral,
    ]);
}*/
#[Route('/panier/pdf', name: 'panier_pdf', methods: ['GET'])]
    public function generatePdf(PanierRepository $panierRepository): Response
    {
        // Récupérer les produits du panier
        $paniers = $panierRepository->findAll();
        $totalGeneral = array_reduce($paniers, function ($total, $panier) {
            return $total + $panier->getTotale();
        }, 0);

        // Créer le contenu HTML pour le PDF
        $html = $this->renderView('panier/pdf.html.twig', [
            'paniers' => $paniers,
            'totalGeneral' => $totalGeneral
        ]);

        // Configurer DomPDF
        $options = new Options();
        $options->set('isHtml5ParserEnabled', true);
        $options->set('isPhpEnabled', true);
        $dompdf = new Dompdf($options);

        // Charger le contenu HTML dans DomPDF
        $dompdf->loadHtml($html);

        // (Facultatif) Configurer la taille du papier
        $dompdf->setPaper('A4', 'portrait');

        // Rendre le PDF
        $dompdf->render();

        // Retourner le PDF en réponse
        return new Response(
            $dompdf->output(),
            200,
            [
                'Content-Type' => 'application/pdf',
                'Content-Disposition' => 'inline; filename="panier.pdf"',
            ]
        );
    }


    

   
}

