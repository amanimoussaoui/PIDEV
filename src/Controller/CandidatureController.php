<?php

namespace App\Controller;

use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\Routing\Attribute\Route;
use App\Repository\CandidatureRepository;
use App\Form\CandidatureType;
use Doctrine\ORM\EntityManagerInterface;
use Doctrine\Persistence\ManagerRegistry;
use Symfony\Component\Validator\Validator\ValidatorInterface;
use Symfony\Bundle\SecurityBundle\Security;
use Symfony\Component\Security\Core\Authentication\Token\Storage\TokenStorageInterface;

use App\Repository\TerrainRepository;
use App\Entity\Candidature;

use App\Entity\Terrain;



class CandidatureController extends AbstractController
{
    #[Route('/candidature', name: 'app_candidature')]
    public function index(): Response
    {
        return $this->render('candidature/index.html.twig', [
            'controller_name' => 'CandidatureController',
        ]);
    }

    //SHOW TABLE DE LA BASE DE DONNEE
    #[Route('/showcandidature', name: 'app_showcandidature')]
    public function showcandidature(CandidatureRepository $a): Response
    {
        $candidature = $a->findAll();
        return $this->render('candidature/showcandidature.html.twig', [
            'tab_candidature' => $candidature,
        ]);
    }

    //DELETE
    #[Route('/candidature/delete/{id}', name: 'app_deletecandidature')]
    public function deletecandidature(int $id, EntityManagerInterface $entityManager): Response
    {
        $candidature = $entityManager->getRepository(Candidature::class)->find($id);

        if (!$candidature) {
            throw $this->createNotFoundException('Candidature non trouvée');
        }

        $entityManager->remove($candidature);
        $entityManager->flush();

        return $this->redirectToRoute('app_showcandidature');
    }

    //AJOUT
    #[Route('/addformcandidature/{idTerrain}', name: 'app_addformcandidature')]
    public function ajouterCandidature(
        Request $request, 
        EntityManagerInterface $entityManager, 
        TerrainRepository $terrainRepository, 
        int $idTerrain
    ) {
        // Récupérer l'objet terrain à partir de l'ID
        $terrain = $entityManager->getRepository(Terrain::class)->find($idTerrain);
        
        // Vérifier si le terrain existe
        if (!$terrain) {
            throw $this->createNotFoundException('Terrain non trouvé');
        }
    
        // Créer une nouvelle candidature
        $candidature = new Candidature();
        $candidature->setIdTerrain($terrain);
        $candidature->setMontant($terrain->getPrix());
    
        // Créer le formulaire
        $form = $this->createForm(CandidatureType::class, $candidature, [
            'terrains' => $terrainRepository->findAll(),
        ]);
    
        $form->handleRequest($request);
    
        if ($form->isSubmitted() && $form->isValid()) {
            $entityManager->persist($candidature);
            $entityManager->flush();
            return $this->redirectToRoute('app_showcandidature');
        }
    
        // Rendre la vue avec les variables nécessaires
       return $this->render('candidature/addformcandidature.html.twig', [
    'formadd' => $form->createView(),
    'terrain' => $terrain,  // Passez l'objet terrain à la vue
]);
    }
    


//UPDATE FROM FORMULAIRE

#[Route('/updateformcandidature/{id}', name: 'app_updateformcandidature')]
public function updateformcandidature(ManagerRegistry $m, Request $req, $id, CandidatureRepository $rep): Response
{
    $em = $m->getManager();
    $candidature = $rep->find($id);

    // Sauvegarder les anciennes valeurs pour les champs que vous ne voulez pas modifier
    $oldMontant = $candidature->getMontant();
    $oldIdTerrain = $candidature->getIdTerrain();

    // Créer le formulaire et y passer les terrains
    $form = $this->createForm(CandidatureType::class, $candidature, [
        'terrains' => $m->getRepository(Terrain::class)->findAll(),
    ]);

    $form->handleRequest($req);
    
    if ($form->isSubmitted() && $form->isValid()) {
        // Remettre les anciennes valeurs pour les champs non modifiés
        $candidature->setMontant($oldMontant);
        $candidature->setIdTerrain($oldIdTerrain);

        $em->persist($candidature);
        $em->flush();

        return $this->redirectToRoute('app_showcandidature');
    }

    return $this->render('candidature/addformcandidature.html.twig', [
        'formadd' => $form->createView(),
    ]);
}



}
