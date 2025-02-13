<?php

namespace App\Controller;

use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\Routing\Annotation\Route;
use App\Repository\MachineRepository;
use App\Entity\Machine;
use App\Form\MachineType;
use Doctrine\Persistence\ManagerRegistry;
class GestionMachineController extends AbstractController
{
    // Route pour afficher la liste des machines
    #[Route('/gestion/machines', name: 'gestion_machine_index')]
    public function index(): Response
    {
        return $this->render('gestion_machine/index.html.twig', [
            'controller_name' => 'GestionMachineController',
        ]);
    }

    // Route pour afficher toutes les machines
    #[Route('/gestion/showmachines', name: 'gestion_machine_show')]
    public function showMachines(MachineRepository $repo): Response
    {
        $machines = $repo->findAll(); // Récupère toutes les machines
        return $this->render('gestion_machine/show_machines.html.twig', [
            'tabmachines' => $machines, // Passe les machines à la vue
        ]);
    }
    

    // Route pour ajouter une machine avec des données par défaut
    #[Route('/gestion/addmachine', name: 'gestion_machine_add')]
   

    // Route pour afficher un formulaire d'ajout de machine
    #[Route('/gestion/addformmachine', name: 'gestion_machine_addform')]
    public function addFormMachine(ManagerRegistry $doctrine, Request $request): Response
    {
        $em = $doctrine->getManager();
        $machine = new Machine();
        $form = $this->createForm(MachineType::class, $machine); // Crée le formulaire pour la machine
        $form->handleRequest($request);

        // Si le formulaire est soumis et valide
        if ($form->isSubmitted() && $form->isValid()) {
            $em->persist($machine);
            $em->flush();
        
            return $this->redirect($this->generateUrl('gestion_machine_show')); // Redirige vers la liste des machines
        }

        return $this->render('gestion_machine/addformmachine.html.twig', [
            'formadd' => $form, // Passe la vue du formulaire à la vue Twig
        ]);
    }

    // Route pour mettre à jour une machine existante
    #[Route('/gestion/updatemachine/{id}', name: 'gestion_machine_update')]
    public function updateMachine(ManagerRegistry $doctrine, Request $request, $id, MachineRepository $repo): Response
    {
        $em = $doctrine->getManager();
        $machine = $repo->find($id); // Récupère la machine par son ID

        if (!$machine) {
            throw $this->createNotFoundException('Machine non trouvée');
        }

        $form = $this->createForm(MachineType::class, $machine); // Crée le formulaire de mise à jour
        $form->handleRequest($request);

        // Si le formulaire est soumis et valide
        if ($form->isSubmitted() && $form->isValid()) {
            $em->flush(); // Enregistre les modifications

            return $this->redirectToRoute('gestion_machine_show'); // Redirige vers la liste des machines
        }

        return $this->render('gestion_machine/addformmachine.html.twig', [
            'formadd' => $form->createView(), // Passe la vue du formulaire à la vue Twig
        ]);
    }

    // Route pour supprimer une machine
    #[Route('/gestion/deletemachine/{id}', name: 'gestion_machine_delete')]
    public function deleteMachine(ManagerRegistry $doctrine, $id, MachineRepository $repo): Response
    {
        $em = $doctrine->getManager();
        $machine = $repo->find($id); // Récupère la machine par son ID

        if ($machine) {
            $em->remove($machine); // Supprime la machine
            $em->flush(); // Enregistre la suppression
        }

        return $this->redirectToRoute('gestion_machine_show'); // Redirige vers la liste des machines
    }
    #[Route('/gestion/materiel', name: 'gestion_materiel')]

#[Route('/gestion/showmachinesfront', name: 'gestion_machine_show_front')]
public function showMachinesfront(MachineRepository $repo): Response
{
    $machines = $repo->findAll(); // Récupère toutes les machines
    return $this->render('gestion_machine/show_machine_front.html.twig', [
        'tabmachines' => $machines, // Passe les machines à la vue
    ]);
}
#[Route('/gestion/showmachinesfront2', name: 'gestion_machine_show_front2')]
public function showMachinesfront2(MachineRepository $repo): Response
{
    $machines = $repo->findBy(['disponibilite' => 'oui']);
    return $this->render('gestion_machine/show_machine_front2.html.twig', [
        'tabmachines' => $machines, // Passe les machines à la vue
    ]);
}
#[Route('/gestion/addformmachinefront', name: 'gestion_machine_addformfront')]
public function addFormMachinefront(ManagerRegistry $doctrine, Request $request): Response
{
    $em = $doctrine->getManager();
    $machine = new Machine();
    $form = $this->createForm(MachineType::class, $machine); // Crée le formulaire pour la machine
    $form->handleRequest($request);

    // Si le formulaire est soumis et valide
    if ($form->isSubmitted() && $form->isValid()) {
        $em->persist($machine);
        $em->flush();
    
        return $this->redirect($this->generateUrl('gestion_machine_show_front')); // Redirige vers la liste des machines
    }

    return $this->render('gestion_machine/addformmachinefront.html.twig', [
        'formadd' => $form, // Passe la vue du formulaire à la vue Twig
    ]);
}
   #[Route('/gestion/updatemachinefront/{id}', name: 'gestion_machine_updatefront')]
    public function updateMachinefront(ManagerRegistry $doctrine, Request $request, $id, MachineRepository $repo): Response
    {
        $em = $doctrine->getManager();
        $machine = $repo->find($id); // Récupère la machine par son ID

        if (!$machine) {
            throw $this->createNotFoundException('Machine non trouvée');
        }

        $form = $this->createForm(MachineType::class, $machine); // Crée le formulaire de mise à jour
        $form->handleRequest($request);

        // Si le formulaire est soumis et valide
        if ($form->isSubmitted() && $form->isValid()) {
            $em->flush(); // Enregistre les modifications

            return $this->redirectToRoute('gestion_machine_show_front'); // Redirige vers la liste des machines
        }

        return $this->render('gestion_machine/addformmachinefront.html.twig', [
            'formadd' => $form->createView(), // Passe la vue du formulaire à la vue Twig
        ]);
    }

    // Route pour supprimer une machine
    #[Route('/gestion/deletemachinefront/{id}', name: 'gestion_machine_deletefront')]
    public function deleteMachinefront(ManagerRegistry $doctrine, $id, MachineRepository $repo): Response
    {
        $em = $doctrine->getManager();
        $machine = $repo->find($id); // Récupère la machine par son ID

        if ($machine) {
            $em->remove($machine); // Supprime la machine
            $em->flush(); // Enregistre la suppression
        }

        return $this->redirectToRoute('gestion_machine_show_front'); // Redirige vers la liste des machines
    }
    #[Route('/gestion/materiel', name: 'gestion_materiel')]
public function gestionMateriel(): Response
{
    return $this->render('gestion_machine/initiale_front.html.twig');
}
}
