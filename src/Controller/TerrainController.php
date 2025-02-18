<?php

// src/Controller/TerrainController.php
namespace App\Controller;

use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\Routing\Attribute\Route;
use App\Repository\TerrainRepository;
use App\Entity\Terrain;//nom de l'entité

use App\Form\TerrainType;//nom du form
use Doctrine\ORM\EntityManagerInterface;
use Doctrine\Persistence\ManagerRegistry;
use Symfony\Component\Validator\Validator\ValidatorInterface;
use Symfony\Component\Security\Core\Authentication\Token\Storage\TokenStorageInterface;

use Symfony\Component\Security\Core\Security;
use Symfony\Component\HttpFoundation\File\UploadedFile;
use Symfony\Component\DependencyInjection\Attribute\Autowire;





class TerrainController extends AbstractController
{
    /**
     * Afficher tous les terrains
     * 
     * @Route("/terrains", name="terrain_index")
     */
    public function index(TerrainRepository $terrainRepository): Response
    {
        $terrains = $terrainRepository->findAll();

        return $this->render('terrain/index.html.twig', [
            'terrains' => $terrains,
        ]);
    }

   //SHOW TABLE DE LA BASE DE DONNEE

#[Route('/showterrain', name: 'app_showterrain')]
public function showterrain(TerrainRepository  $a): Response
{
    $terrain = $a->findAll();
    return $this->render('terrain/showterrain.html.twig', [
        'tab_terrain' => $terrain,
    ]);
}


//AJOUT  VIA  FORMULAIRE

#[Route('/addformterrain', name: 'app_addformterrain')]
public function addformterrain(ManagerRegistry $m, Request $req): Response
    {
        $em = $m->getManager();
        $terrain = new Terrain();
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
 public function listTerrains()
 {
     // Récupérer les terrains depuis la base de données
     $terrains = $this->entityManager->getRepository(Terrain::class)->findAll();

     // Passer les terrains à la vue
     return $this->render('terrain/affichage.html.twig', [
         'tab_terrain' => $terrains,
     ]);
 }

 #[Route('/terrain/{id}', name: 'app_affichage_terrain')]
public function afficherTerrain(int $id, TerrainRepository $repository): Response
{
    $terrain = $repository->find($id);
    
    if (!$terrain) {
        throw $this->createNotFoundException('Terrain non trouvé.');
    }

    return $this->render('terrain/details.html.twig', [
        'terrain' => $terrain,
    ]);
}
}