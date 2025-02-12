<?php

namespace App\Controller;

use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\HttpFoundation\Request;
use App\Repository\TerrainRepository;
use App\Entity\Terrain;//nom de l'entité
use App\Form\TerrainType;//nom du form
use Doctrine\ORM\EntityManagerInterface;
use Doctrine\Persistence\ManagerRegistry;
use Symfony\Component\Validator\Validator\ValidatorInterface;


final class AcceuilBackController extends AbstractController
{
    #[Route('/dashboard', name: 'app_dashboard')]
    public function dashboard(): Response
    {
        return $this->render('acceuil_back/dashboard.html.twig', [
            'controller_name' => 'AcceuilBackController',
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
        // Vérifier que l'URL de l'image est valide
        if (!filter_var($terrain->getImage(), FILTER_VALIDATE_URL)) {
            $this->addFlash('error', 'Veuillez entrer une URL d\'image valide.');
            return $this->redirectToRoute('app_addformterrain'); // Redirection en cas d'erreur
        }

        $em->persist($terrain);
        $em->flush();

        $this->addFlash('success', 'Terrain ajouté avec succès !');
        return $this->redirectToRoute('app_showterrain'); // Redirection après ajout
    }

    return $this->render('terrain/addformterrain.html.twig', [
        'formadd' => $form,
    ]);
}

//UPDATE FROM FORMULAIRE

#[Route('/updateformterrain/{id}', name: 'app_updateformterrain')]
    public function updateformterrain(ManagerRegistry $m,Request $req,$id,TerrainRepository $rep): Response
    {
        $em = $m->getManager();
        $terrain = $rep->find($id);
        $form = $this->createForm(TerrainType::class, $terrain);
        $form->handleRequest($req);
        if ($form->isSubmitted() && $form->isValid()) {
            $em->persist($terrain);
            $em->flush();
            return $this->redirectToRoute('app_showterrain');
        }
        return $this->render('terrain/addformterrain.html.twig', [
            'formadd' =>$form ,
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
}
