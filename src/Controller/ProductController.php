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
use Knp\Component\Pager\PaginatorInterface;

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
            $product->setUtilisateurs($this->getUser());
            $entityManager->persist($product);
            $entityManager->flush();
            return $this->redirectToRoute('product_list');
        }

        return $this->render('product/new.html.twig', [
            'form' => $form->createView(),
        ]);
    }

    #[Route('/', name: 'product_list', methods: ['GET'])]
    public function index(Request $request, EntityManagerInterface $entityManager, PaginatorInterface $paginator): Response
    {
        $user = $this->getUser();
        $queryBuilder = $entityManager->getRepository(Product::class)->createQueryBuilder('p')
            ->leftJoin('p.utilisateurs', 'u')
            ->addSelect('u');

        if ($user) {
            $queryBuilder->where('u = :user')
                ->setParameter('user', $user);
        }

        $products = $paginator->paginate(
            $queryBuilder->getQuery(),
            $request->query->getInt('page', 1),
            10 // Nombre de produits par page
        );

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
            $entityManager->flush();
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
        return $this->redirectToRoute('product_list');
    }

    #[Route('/products', name: 'product_front_list', methods: ['GET'])]
    public function showProductsFront(Request $request, PaginatorInterface $paginator, EntityManagerInterface $entityManager): Response
    {
        $queryBuilder = $entityManager->getRepository(Product::class)->createQueryBuilder('p');
        $products = $paginator->paginate(
            $queryBuilder->getQuery(),
            $request->query->getInt('page', 1),
            3
        );

        return $this->render('product/showProductsFront.html.twig', [
            'products' => $products,
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

    #[Route('/product/produit_client/{id}', name: 'product_details_client')]
    public function details_client($id, ProductRepository $produitRepository): Response
    {
        $products = $produitRepository->find($id);

        return $this->render('product/details_client.html.twig', [
            'product' => $products,
        ]);
    }

    #[Route('/search', name: 'product_search', methods: ['GET'])]
public function search(Request $request, ProductRepository $productRepository, PaginatorInterface $paginator): Response
{
    $category = $request->query->get('category');
   
    $sort = $request->query->get('sort', 'default'); // Valeur par défaut

    $criteria = [];

    if ($category) {
        $criteria['category'] = $category;
    }
   
    $qb = $productRepository->createQueryBuilder('p');

    if (isset($criteria['category'])) {
        $qb->andWhere('p.category = :category')
            ->setParameter('category', $criteria['category']);
    }

   
    // Pagination
    $products = $paginator->paginate(
        $qb->getQuery(),
        $request->query->getInt('page', 1),
        3
    );

    return $this->render('product/showProductsFront.html.twig', [
        'products' => $products,
    ]);
}



    #[Route('/product/stats', name: 'product_stats')]
    public function productStats(ProductRepository $productRepository): Response
    {
        $products = $productRepository->findAll();
        $stats = [];

        foreach ($products as $product) {
            $category = $product->getCategory();
            if (!isset($stats[$category])) {
                $stats[$category] = 0;
            }
            $stats[$category]++;
        }

        return $this->render('product/stats.html.twig', [
            'stats' => $stats,
        ]);
    }

    #[Route('/product/recherche', name: 'product_recherche', methods: ['GET'])]
    public function rechercher(ProductRepository $productRepository, Request $request): Response
    {
        $query = $request->query->get('query', '');
        $products = [];

        if (!empty($query)) {
            $products = $productRepository->createQueryBuilder('p')
                ->where('p.nom LIKE :query')
                ->orWhere('p.category LIKE :query')
                ->orWhere('p.prix LIKE :query')
                ->orWhere('p.stock LIKE :query')
                ->setParameter('query', "%$query%")
                ->getQuery()
                ->getResult();
        }

        return $this->render('product/recherche.html.twig', [
            'products' => $products,
            'query' => $query,
        ]);
    }
}