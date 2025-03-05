<?php

namespace App\Controller;
use Stripe\Stripe;
use App\Entity\Panier;
use App\Entity\Product;
use App\Entity\Commande;
use App\Service\StripeService;
use Stripe\Checkout\Session;
use App\Repository\PanierRepository;
use App\Repository\CommandeRepository;
use App\Repository\ProductRepository;
use App\Repository\UtilisateursRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;
use Dompdf\Dompdf;
use Dompdf\Options;
use Symfony\Component\Security\Core\Authentication\Token\Storage\TokenStorageInterface;
use Symfony\Bundle\SecurityBundle\Security;
use App\Service\SmsService;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Session\SessionInterface;
use Symfony\Component\DependencyInjection\ParameterBag\ParameterBagInterface;



#[Route('/panier')]
class PanierController extends AbstractController
{
    private $tokenStorage;
    private $stripePublicKey;

    public function __construct(TokenStorageInterface $tokenStorage,ParameterBagInterface $params)
    {     

        $this->tokenStorage = $tokenStorage;
        $this->stripePublicKey = $params->get('stripe_public_key');

    }
    #[Route('/panier', name: 'app_panier')]
    public function ajouterr($id)
    {
        return $this->render('panier/index.html.twig', [
            'stripe_public_key' => $this->stripePublicKey
        ]);
    }
    /**
     * Récupérez les paniers à partir des commandes liées à l'utilisateur
     */
    #[Route('/', name: 'panier_index', methods: ['GET'])]
    public function index(PanierRepository $panierRepository): Response
    {
        // Vérifiez que l'utilisateur est connecté
        $user = $this->getUser();

        if (!$user) {
            // Si l'utilisateur n'est pas connecté, vous pouvez rediriger vers la page de connexion
            return $this->redirectToRoute('app_login');
        }

        // Récupérer les paniers associés à l'utilisateur via ses commandes
        $paniers = $panierRepository->findByUser($user);

        // Calculer le total général des paniers de l'utilisateur
        $totalGeneral = array_reduce($paniers, function ($total, $panier) {
            return $total + $panier->getTotale();
        }, 0);

        return $this->render('panier/index.html.twig', [
            'paniers' => $paniers,
            'totalGeneral' => $totalGeneral,
           'stripe_public_key' => $this->getParameter('stripe_public_key')

        ]);
    }

    /**
     * Ajouter un produit au panier
     */
    #[Route('/ajouter/{id}', name: 'panier_ajouter', methods: ['GET', 'POST'])]
    public function ajouter(
        Product $product, 
        EntityManagerInterface $entityManager, 
        CommandeRepository $commandeRepository,
        Security $security
    ): Response {
        // 1️⃣ Récupérer l'utilisateur connecté
        $user = $security->getUser();
        if (!$user) {
            $this->addFlash('error', 'Vous devez être connecté pour ajouter un produit au panier.');
            return $this->redirectToRoute('app_login');
        }
    
        // 2️⃣ Vérifier si l'utilisateur a une commande en cours (non finalisée)
        $commande = $commandeRepository->findOneBy(['utilisateurs' => $user]);
    
        // 3️⃣ Si aucune commande en cours, en créer une
        if (!$commande) {
            $commande = new Commande();
            $commande->setUtilisateurs($user);
           
            $commande->setDate(new \DateTime());
    
            $entityManager->persist($commande);
            $entityManager->flush();
        }
    
        // 4️⃣ Vérifier si le produit est déjà dans le panier de cette commande
        $panier = $entityManager->getRepository(Panier::class)->findOneBy([
            'product' => $product,
            'commande' => $commande
        ]);
    
        if ($panier) {
            // Si le produit est déjà dans le panier, augmenter la quantité
            $panier->setQuantite($panier->getQuantite() + 1);
            $panier->setTotale($panier->getQuantite() * $product->getPrix());
        } else {
            // Sinon, créer une nouvelle ligne de panier
            $panier = new Panier();
            $panier->setProduct($product);
            $panier->setQuantite(1);
            $panier->setTotale($product->getPrix());
            $panier->setCommande($commande); // 🔥 Associer le panier à la commande
    
            $entityManager->persist($panier);
        }
    
        // 5️⃣ Enregistrer les modifications
        $entityManager->flush();
    
        // 6️⃣ Rediriger vers le panier
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
            'stripe_public_key' => $this->getParameter('stripe_public_key')

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
    #[Route('/panier/pdf', name: 'panier_pdf', methods: ['GET'])]
    public function generatePdf(PanierRepository $panierRepository): Response
    {
        // Récupérer les produits du panier avec les utilisateurs
        $paniers = $panierRepository->findAll();
        $totalGeneral = array_reduce($paniers, function ($total, $panier) {
            return $total + $panier->getTotale();
        }, 0);
    
        // Générer le chemin absolu pour l'image du logo
        $logoPath = $this->getParameter('kernel.project_dir') . '/public/uploads/images/logo.png';
        $logoUrl = 'file://' . realpath($logoPath); // Utilisation de realpath pour obtenir le chemin absolu
    
        // Créer le contenu HTML pour le PDF avec les données dynamiques
        $html = $this->renderView('panier/pdf.html.twig', [
            'paniers' => $paniers,
            'totalGeneral' => $totalGeneral,
            'logoUrl' => $logoUrl,  // Passer l'URL de l'image
        ]);
    
        // Configurer DomPDF
        $options = new Options();
        $options->set('isHtml5ParserEnabled', true);
        $options->set('isPhpEnabled', true);
        $options->set('isRemoteEnabled', true); // Permet les images distantes
    
        $dompdf = new Dompdf($options);
    
        // Charger le contenu HTML dans DomPDF
        $dompdf->loadHtml($html);
    
        // Configurer la taille du papier
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

    
    #[Route('/dashboard', name: 'panier_dashboard', methods: ['GET'])]
    public function dashboard(PanierRepository $panierRepository, ProductRepository $productRepository): Response
    {
        // 1. Récupérer les paniers et commandes
        $paniers = $panierRepository->findAll();
        $commandes = $panierRepository->findBy([], ['commande' => 'ASC']); // Récupérer les commandes pour un tri

        // 2. Préparer les données pour les graphiques
        $productSales = [];
        $categorySales = [];
        $dateSales = [];

        // Récupérer les ventes par produit
        foreach ($paniers as $panier) {
            $product = $panier->getProduct();
            $productName = $product->getNom();
            $sales = $panier->getTotale();

            if (!isset($productSales[$productName])) {
                $productSales[$productName] = 0;
            }

            $productSales[$productName] += $sales;
        }

        // Récupérer les ventes par date (jour)
       // Vérifier que $commandes contient bien des objets Commande
/*foreach ($commandes as $commande) {
    
    if (!$commande instanceof Commande) {
        throw new \Exception("L'objet dans \$commandes n'est pas une instance de Commande");
    }

    $date = $commande->getDate();
    if ($date instanceof \DateTimeInterface) {
        $formattedDate = $date->format('Y-m-d');
        
        if (!isset($dateSales[$formattedDate])) {
            $dateSales[$formattedDate] = 0;
        }

        // Ajouter les totaux des paniers de la commande
        foreach ($commande->getPaniers() as $panier) {
            $dateSales[$formattedDate] += $panier->getTotale();
        }
    } else {
        throw new \Exception("La date de la commande est invalide.");
    }
}*/


        // Récupérer les ventes par catégorie
       foreach ($paniers as $panier) {
            $product = $panier->getProduct();
            $category = $product->getCategory(); // Cela retourne directement la chaîne de caractères
            $sales = $panier->getTotale();

            if (!isset($categorySales[$category])) {
                $categorySales[$category] = 0;
            }

            $categorySales[$category] += $sales;
        }

        // Passer les données aux graphiques
        return $this->render('panier/dashboard.html.twig', [
            'productSales' => $productSales,
            'categorySales' => $categorySales,
            'dateSales' => $dateSales,
        ]);
    }
    
    #[Route('/panier/trier/{ordre}', name:'panier_trier', methods:['GET'])]

   public function trier(PanierRepository $panierRepository, $ordre = 'asc'): Response
   {
       // Trie des paniers par total montant (ascendant ou descendant)
       $paniers = $panierRepository->findBy([], ['totale' => $ordre]);

       return $this->render('panier/admin_index.html.twig', [
           'paniers' => $paniers
       ]);
   }         

    
    
   }

