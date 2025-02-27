<?php

namespace App\Controller;

use App\Entity\Commande;
use App\Repository\ProductRepository;
use App\Entity\Product;
use App\Entity\Panier;
use App\Form\CommandeType;
use App\Form\EntityType;

use App\Repository\CommandeRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Doctrine\Persistence\ManagerRegistry;
#[Route('/commande')]
class CommandeController extends AbstractController
{
    #[Route('/commande/{productId}', name: 'commande_create')]
    
    public function create(Request $request, EntityManagerInterface $em, int $productId)
    {
        $product = $em->getRepository(Product::class)->find($productId);
       

    // Créer une nouvelle commande
    $commande = new Commande();
    $commande->setUtilisateurs($this->getUser());

    // Créer le formulaire
    $form = $this->createForm(CommandeType::class, $commande);
    $form->handleRequest($request);

    // Si le formulaire est soumis et valide
    if ($form->isSubmitted() && $form->isValid()) {
        // Vérifier si la date est nulle et la définir
        if ($commande->getDate() === null) {
            $commande->setDate(new \DateTime());
        }

        // Enregistrer la commande dans la base de données
        $em->persist($commande);
        $em->flush();
        return $this->redirectToRoute('panier_ajouter', ['id' => $product->getId()]);
    } 

    

    // Passer le produit à la vue
    return $this->render('commande/new.html.twig', [
        'form' => $form->createView(),
        'product' => $product // Passer l'objet Product à la vue
    ]);
}
       
#[Route('/list', name: 'commande_list')]
    public function show(CommandeRepository $commandeRepository): Response
    {
        
         // Récupérer l'utilisateur connecté
    $utilisateur = $this->getUser();

    // Vérifier si l'utilisateur est bien connecté
    if (!$utilisateur) {
        throw $this->createAccessDeniedException('Vous devez être connecté pour voir vos commandes.');
    }

    // Récupérer les commandes de l'utilisateur
    $commandes = $commandeRepository->findAll();

    return $this->render('commande/list._admin.html.twig', [
        'utilisateur' => $utilisateur,
        'commandes' => $commandes,
    ]);
    }

    #[Route('/delete/{id}', name: 'commande_delete')]
    public function delete(Commande $commande, EntityManagerInterface $entityManager): Response
    {
        $entityManager->remove($commande);
        $entityManager->flush();

        return $this->redirectToRoute('commande_list');
    }
    #[Route('/list_client', name: 'commande_list_client')]
    public function show_client(CommandeRepository $commandeRepository): Response
    {

    // Récupérer l'utilisateur connecté
    $utilisateur = $this->getUser();

    // Vérifier si l'utilisateur est bien connecté
    if (!$utilisateur) {
        throw $this->createAccessDeniedException('Vous devez être connecté pour voir vos commandes.');
    }

    // Récupérer les commandes de l'utilisateur
    $commandes = $commandeRepository->findBy(['utilisateurs' => $utilisateur]);

    return $this->render('commande/list_client.html.twig', [
        'utilisateur' => $utilisateur,
        'commandes' => $commandes,
    ]);
    }
    #[Route('/delete_client/{id}', name: 'commande_delete_client')]
    public function delete_client(Commande $commande, EntityManagerInterface $entityManager): Response
    {
        $entityManager->remove($commande);
        $entityManager->flush();

        return $this->redirectToRoute('commande_list_client');
    }

   
    #[Route('/update-commande/{id}', name: 'commande_edit', methods: ['GET', 'POST'])]
    public function updateCommande(Request $request, EntityManagerInterface $entityManager, CommandeRepository $commandeRepository, int $id): Response
    {
        // Récupérer la commande avec l'ID
        $commande = $commandeRepository->find($id);

        if (!$commande) {
            throw $this->createNotFoundException('Commande non trouvée');
        }

        // Créer le formulaire pour modifier la commande
        $form = $this->createForm(CommandeType::class, $commande);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            // Enregistrer les modifications en base de données
            $entityManager->flush();

            // Message flash de confirmation
            $this->addFlash('success', 'Commande mise à jour avec succès !');

            // Rediriger vers la liste des commandes
            return $this->redirectToRoute('commande_list_client');
        }

        return $this->render('commande/edit.html.twig', [
            'form' => $form->createView(),
        ]);
    }


    }
    
