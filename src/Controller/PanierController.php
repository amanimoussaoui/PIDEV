<?php

namespace App\Controller;

use App\Entity\Panier;
use App\Entity\Product;
use App\Repository\PanierRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

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
    

   
}

