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
use App\Repository\UtilisateurRepository;
use App\Repository\TerrainRepository;
use App\Entity\Candidature;
use App\Entity\Utilisateur;

final class CandidatureController extends AbstractController
{
    #[Route('/candidature', name: 'app_candidature')]
    public function index(): Response
    {
        return $this->render('candidature/index.html.twig', [
            'controller_name' => 'CandidatureController',
        ]);
    }

    #[Route('/candidature/add/{idTerrain}/{idUser}', name: 'app_add_candidature')]
    public function addCandidature(
        int $idTerrain,
        int $idUser,
        TerrainRepository $terrainRepository,
        UtilisateurRepository $utilisateurRepository,
        EntityManagerInterface $entityManager
    ): Response {
        // Récupérer le terrain
        $terrain = $terrainRepository->find($idTerrain);
        if (!$terrain) {
            throw $this->createNotFoundException('Terrain non trouvé.');
        }

        // Récupérer l'utilisateur (via son ID)
        $utilisateur = $utilisateurRepository->find($idUser);
        if (!$utilisateur) {
            throw $this->createNotFoundException('Utilisateur non trouvé.');
        }

        // Créer une nouvelle candidature
        $candidature = new Candidature();
        $candidature->setDate(new \DateTime()); // Date actuelle
        $candidature->setMontant($terrain->getPrix()); // Prix du terrain
        $candidature->setEtat('en attente'); // État initial
        $candidature->setIdUser($utilisateur); // Utilisateur qui postule
        $candidature->setIdTerrain($terrain); // Terrain concerné

        // Sauvegarde dans la base de données
        $entityManager->persist($candidature);
        $entityManager->flush();

        // Ajouter le message flash de succès
        $this->addFlash('success', 'Votre candidature a été enregistrée avec succès.');

        // Rediriger vers la même page pour afficher le message
        return $this->redirectToRoute('app_affichage_terrain', ['id' => $terrain->getId()]);
    }

       //SHOW TABLE DE LA BASE DE DONNEE

#[Route('/showcandidature', name: 'app_showcandidature')]
public function showcandidature(CandidatureRepository  $a): Response
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
         throw $this->createNotFoundException('candidature non trouvé');
     }
 
     $entityManager->remove($candidature);
     $entityManager->flush();
 
     return $this->redirectToRoute('app_showcandidature');
 }
}
