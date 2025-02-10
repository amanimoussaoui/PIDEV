<?php

namespace App\Controller;

use App\Entity\Product;
use App\Form\ProductType;
use App\Repository\ProductRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bridge\Doctrine\ManagerRegistry;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

#[Route('/product')]
class ProductController extends AbstractController
{
    #[Route('/new', name: 'product_new', methods: ['GET', 'POST'])]
    public function new(Request $request, EntityManagerInterface $entityManager): Response
    {
        $product = new Product();
        $form = $this->createForm(ProductType::class, $product);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            // Enregistrement de l'entité dans la base de données
            $entityManager->persist($product);
            $entityManager->flush();

            // Message de succès
            $this->addFlash('success', 'Produit ajouté avec succès !');

            // Redirection vers la liste des produits (ou autre page)
            return $this->redirectToRoute('product_list');
        }

        return $this->render('product/new.html.twig', [
            'form' => $form->createView(),
        ]);
    }
    #[Route('/', name: 'product_list', methods: ['GET'])]
public function index(EntityManagerInterface $entityManager): Response
{
    $products = $entityManager->getRepository(Product::class)->findAll();

    return $this->render('product/index.html.twig', [
        'products' => $products,
    ]);
}
#[Route('/update/{id}', name: 'product_update', methods: ['GET', 'POST'])]
public function updateProduct(Request $request, EntityManagerInterface $entityManager, ProductRepository $productRepository, int $id): Response
{
    $product = $productRepository->find($id);

    if (!$product) {
        throw $this->createNotFoundException('Produit non trouvé');
    }

    $form = $this->createForm(ProductType::class, $product);
    $form->handleRequest($request);

    if ($form->isSubmitted() && $form->isValid()) {
        $entityManager->flush(); // Pas besoin de persist, car c'est une mise à jour

        $this->addFlash('success', 'Produit mis à jour avec succès !');

        return $this->redirectToRoute('product_list');
    }

    return $this->render('product/new.html.twig', [
        'form' => $form->createView(),
    ]);
}
#[Route('/delete/{id}', name: 'product_delete', methods: ['GET', 'POST'])]
public function deleteProduct($id, EntityManagerInterface $entityManager, ProductRepository $productRepository): Response
{
    $product = $productRepository->find($id);

    if (!$product) {
        throw $this->createNotFoundException('Produit non trouvé');
    }

    $entityManager->remove($product);
    $entityManager->flush();

    $this->addFlash('success', 'Produit supprimé avec succès !');

    return $this->redirectToRoute('product_list');
}
#[Route('/products', name: 'product_front_list', methods: ['GET'])]
public function showProductsFront(EntityManagerInterface $entityManager): Response
{
    // Récupérer tous les produits depuis la base de données
    $products = $entityManager->getRepository(Product::class)->findAll();

    // Rendu de la vue en passant les produits
    return $this->render('product/showProductsFront.html.twig', [
        'products' => $products,  // On passe les produits à la vue
    ]);
}









}
