<?php

namespace App\Controller;

use App\Entity\Product;
use App\Entity\Panier;

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
            // Associer l'utilisateur connecté au produit
            $product->setUtilisateurs($this->getUser());

            // Enregistrement de l'entité dans la base de données
            $entityManager->persist($product);
            $entityManager->flush();

            // Message de succès
            $this->addFlash('success', 'Produit ajouté avec succès !');

            // Redirection vers la liste des produits
            return $this->redirectToRoute('product_list');
        }

        return $this->render('product/new.html.twig', [
            'form' => $form->createView(),
        ]);
    }
    #[Route('/', name: 'product_list', methods: ['GET'])]
public function index(EntityManagerInterface $entityManager): Response
{
   // Récupérer les produits avec la relation utilisateurs
   $products = $entityManager->getRepository(Product::class)
   ->createQueryBuilder('p')
   ->leftJoin('p.utilisateurs', 'u')  // Joindre la relation utilisateurs
   ->addSelect('u')  // Sélectionner l'utilisateur aussi
   ->getQuery()
   ->getResult();

return $this->render('product/index.html.twig', [
   'products' => $products,
    ]);
    
}
#[Route('/admin', name: 'product_list_admin', methods: ['GET'])]
public function index2(EntityManagerInterface $entityManager): Response
{
    $products = $entityManager->getRepository(Product::class)->findAll();

    return $this->render('product/index_admin.html.twig', [
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
#[Route('/delete/{id}', name: 'product_delete_admin', methods: ['GET', 'POST'])]
public function deleteProduct_admin($id, EntityManagerInterface $entityManager, ProductRepository $productRepository): Response
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
#[Route('/product/produit/{id}', name: 'product_details')]
public function details($id, ProductRepository $produitRepository): Response
{
    $products = $produitRepository->find($id);

    if (!$products) {
        throw $this->createNotFoundException('Produit non trouvé');
    }

    return $this->render('product/details.html.twig', [
        'product' => $products,
    ]);
}
#[Route('/search', name: 'product_search', methods: ['GET'])]
public function search(Request $request, ProductRepository $productRepository): Response
{
    $category = $request->query->get('category'); // Récupérer la catégorie depuis l'URL
    $sort = $request->query->get('sort'); // Récupérer le paramètre de tri

    // Récupérer les produits en fonction de la catégorie et du tri
    if ($category) {
        if ($sort === 'price_asc') {
            $products = $productRepository->findBy(['category' => $category], ['prix' => 'ASC']);
        } else {
            $products = $productRepository->findBy(['category' => $category]);
        }
    } else {
        if ($sort === 'price_asc') {
            $products = $productRepository->findBy([], ['prix' => 'ASC']);
        } else {
            $products = $productRepository->findAll();
        }
    }

    return $this->render('product/showProductsFront.html.twig', [
        'products' => $products,
    ]);
}
}
