<?php

// src/Controller/TerrainController.php
namespace App\Controller;

use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\Routing\Attribute\Route;
use App\Repository\TerrainRepository;
use App\Repository\CandidatureRepository;

use App\Entity\Terrain;//nom de l'entité
use App\Entity\Utilisateurs;
use App\Form\TerrainType;//nom du form
use Doctrine\ORM\EntityManagerInterface;
use Doctrine\Persistence\ManagerRegistry;
use Symfony\Component\Validator\Validator\ValidatorInterface;
use Symfony\Component\Security\Core\Authentication\Token\Storage\TokenStorageInterface;

use Symfony\Component\Security\Core\Security;
use Symfony\Component\HttpFoundation\File\UploadedFile;
use Symfony\Component\DependencyInjection\Attribute\Autowire;
use Symfony\Component\Security\Http\Attribute\IsGranted;





class TerrainController extends AbstractController
{
    /**
     * Afficher tous les terrains
     * 
     * @Route("/terrains", name="terrain_index")
     */
    #[Route('/terrain', name: 'app_terrain_index', methods: ['GET'])]
    public function index(TerrainRepository $terrainRepository): Response
    {
        $user = $this->getUser();
    
    if ($this->isGranted('ROLE_ADMIN')) {
        return $this->render('terrain/index.html.twig', [
            'terrains' => $terrainRepository->findAll()
        ]);
    } elseif ($this->isGranted('ROLE_AGRICULTEUR')) {
        return $this->render('terrain/index.html.twig', [
            'terrains' => $terrainRepository->findBy(['user' => $user])
        ]);
    }
    
    return $this->render('terrain/index.html.twig', [
        'terrains' => $terrainRepository->findAll()
    ]);
    }
    // Ajouter un terrain (uniquement pour agriculteur)
    #[Route('/terrain/new', name: 'app_terrain_new', methods: ['GET', 'POST'])]
   #[IsGranted('ROLE_AGRICULTEUR')]
   #[Route('/terrain/new', name: 'app_terrain_new')]
   public function new(Request $request, EntityManagerInterface $entityManager): Response
   {
       $user = $this->getUser(); // Récupérer l'utilisateur connecté
       if (!$user) {
           throw $this->createAccessDeniedException("Vous devez être connecté pour ajouter un terrain.");
       }
   
       $terrain = new Terrain();
       $terrain->setUtilisateur($user); // Associer l'utilisateur connecté au terrain
   
       $form = $this->createForm(TerrainType::class, $terrain);
       $form->handleRequest($request);
   
       if ($form->isSubmitted() && $form->isValid()) {
           $entityManager->persist($terrain);
           $entityManager->flush();
   
           return $this->redirectToRoute('app_showterrain');
       }
   
       return $this->render('terrain/new.html.twig', [
           'form' => $form->createView(),
       ]);
   }
   

#[Route('/mes-terrains', name: 'app_terrain_mine')]
public function mesTerrains(TerrainRepository $terrainRepository): Response
{
    $user = $this->getUser();
    $terrains = $terrainRepository->findBy(['user' => $user]);

    return $this->render('terrain/mes_terrains.html.twig', [
        'terrains' => $terrains,
    ]);
}


   //SHOW TABLE DE LA BASE DE DONNEE

   #[Route('/showterrain', name: 'app_showterrain')]
   public function showterrain(TerrainRepository $terrainRepository): Response
   {
       $user = $this->getUser(); // Récupérer l'utilisateur connecté
       if (!$user) {
           throw $this->createAccessDeniedException("Vous devez être connecté pour voir vos terrains.");
       }
   
       $terrains = $terrainRepository->findBy(['utilisateur' => $user]); // Filtrer par utilisateur
   
       return $this->render('terrain/showterrain.html.twig', [
           'tab_terrain' => $terrains,
       ]);
   }
   

//AJOUT  VIA  FORMULAIRE

#[Route('/addformterrain', name: 'app_addformterrain')]
public function addformterrain(ManagerRegistry $m, Request $req): Response
{
    $user = $this->getUser(); // Récupérer l'utilisateur connecté
    if (!$user) {
        throw $this->createAccessDeniedException("Vous devez être connecté pour ajouter un terrain.");
    }

    $em = $m->getManager();
    $terrain = new Terrain();
    $terrain->setUtilisateur($user); // Associer l'utilisateur connecté

    $form = $this->createForm(TerrainType::class, $terrain);
    $form->handleRequest($req);

    if ($form->isSubmitted() && $form->isValid()) {
        /** @var UploadedFile $imageFile */
        $imageFile = $form->get('image')->getData();
        if ($imageFile) {
            $destination = $this->getParameter('kernel.project_dir').'/public/uploads';
            $newFilename = uniqid().'.'.$imageFile->guessExtension();
            $imageFile->move($destination, $newFilename);
            $terrain->setImage('/uploads/'.$newFilename);
        }

        $em->persist($terrain);
        $em->flush();

        $this->addFlash('success', 'Terrain ajouté avec succès !');
        return $this->redirectToRoute('app_showterrain');
    }

    return $this->render('terrain/addformterrain.html.twig', [
        'formadd' => $form->createView(),
    ]);
}

//UPDATE FROM FORMULAIRE


#[Route('/updateformterrain/{id}', name: 'app_updateformterrain')]
public function updateformterrain(ManagerRegistry $m, Request $req, $id, TerrainRepository $rep, #[Autowire('%uploads_dir%')] string $uploadsDir): Response
{
    $em = $m->getManager();
    $terrain = $rep->find($id);

    if (!$terrain) {
        throw $this->createNotFoundException('Terrain non trouvé');
    }

    // Conservez l'image actuelle avant de tenter de la modifier
    $currentImage = $terrain->getImage();

    $form = $this->createForm(TerrainType::class, $terrain);
    $form->handleRequest($req);

    if ($form->isSubmitted() && $form->isValid()) {
        /** @var UploadedFile $imageFile */
        $imageFile = $form->get('image')->getData();

        if ($imageFile) {
            // Si une nouvelle image est téléchargée
            $newFilename = uniqid().'.'.$imageFile->guessExtension();
            
            // Déplacer le fichier dans le répertoire 'uploads'
            $imageFile->move($uploadsDir, $newFilename);
            
            // Mettre à jour l'image du terrain avec le chemin relatif
            $terrain->setImage('/uploads/'.$newFilename);
        } else {
            // Si aucune nouvelle image n'est téléchargée, garder l'ancienne image
            $terrain->setImage($currentImage);
        }

        // Sauvegarder les modifications
        $em->persist($terrain);
        $em->flush();

        // Message de succès et redirection
        $this->addFlash('success', 'Terrain modifié avec succès !');
        return $this->redirectToRoute('app_showterrain');
    }

    // Passer la variable 'terrain' à la vue Twig
    return $this->render('terrain/addformterrain.html.twig', [
        'formadd' => $form->createView(),
        'current_image' => $currentImage, // Passer l'image actuelle au template
        'terrain' => $terrain, // Passer l'objet 'terrain' à la vue
    ]);
}





    //DELETE FROM FORMULAIRE

 #[Route('/terrain/delete/{id}', name: 'app_deleteformterrain')]
 public function deleteterrain(int $id, EntityManagerInterface $entityManager): Response
 {
     $terrain = $entityManager->getRepository(Terrain::class)->find($id);
 
     if (!$terrain) {
         throw $this->createNotFoundException('terrain non trouvé');
     }
 
     $entityManager->remove($terrain);
     $entityManager->flush();
 
     return $this->redirectToRoute('app_showterrain');
 }
 private $entityManager;

 // Injection de EntityManagerInterface
 public function __construct(EntityManagerInterface $entityManager)
 {
     $this->entityManager = $entityManager;
 }

 #[Route('/terrains', name: 'list_terrains')]
public function listTerrains(): Response
{
    $user = $this->getUser(); // Récupérer l'utilisateur connecté

    if (!$user) {
        throw $this->createAccessDeniedException("Vous devez être connecté pour voir les terrains.");
    }

    // Vérifier le rôle de l'utilisateur
    if (in_array('ROLE_AGRICULTEUR', $user->getRoles())) {
        // Si l'utilisateur est un agriculteur, afficher uniquement ses terrains
        $terrains = $this->entityManager->getRepository(Terrain::class)->findBy(['utilisateur' => $user]);
    } else {
        // Sinon (client ou admin), afficher tous les terrains
        $terrains = $this->entityManager->getRepository(Terrain::class)->findAll();
    }

    return $this->render('terrain/affichage.html.twig', [
        'tab_terrain' => $terrains,
    ]);
}


#[Route('/terrain/{id}', name: 'app_affichage_terrain')]
public function afficherTerrain(int $id, TerrainRepository $terrainRepository, CandidatureRepository $candidatureRepository): Response
{
    $user = $this->getUser(); // Récupérer l'utilisateur connecté

    if (!$user) {
        throw $this->createAccessDeniedException("Vous devez être connecté pour voir ce terrain.");
    }

    // Récupérer le terrain avec l'ID donné
    $terrain = $terrainRepository->find($id);

    if (!$terrain) {
        throw $this->createNotFoundException('Terrain non trouvé.');
    }

    // Récupérer les candidatures de l'utilisateur connecté pour ce terrain
    $candidatures = $candidatureRepository->findBy([
        'utilisateur' => $user,
        'idTerrain' => $terrain, // Utiliser 'idTerrain' ici, pas 'terrain'
    ]);

    return $this->render('terrain/details.html.twig', [
        'terrain' => $terrain,
        'candidatures' => $candidatures, // Passer les candidatures à la vue
    ]);
}


}