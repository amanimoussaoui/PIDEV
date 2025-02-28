<?php

namespace App\Controller;

use App\Entity\Machine;
use App\Repository\MachineRepository;
use App\Form\MachineType;
use Doctrine\Persistence\ManagerRegistry;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;
use Symfony\Component\Security\Http\Attribute\IsGranted;


class GestionMachineController extends AbstractController
{
    #[IsGranted('ROLE_ADMIN')]
    #[Route('/gestion/machines', name: 'gestion_machine_index')]
    public function index(): Response
    {
        return $this->render('gestion_machine/index.html.twig');
    }
    #[IsGranted('ROLE_ADMIN')]
    #[Route('/gestion/showmachines', name: 'gestion_machine_show')]
    public function showMachines(MachineRepository $repo): Response
    {
        return $this->render('gestion_machine/show_machines.html.twig', [
            'tabmachines' => $repo->findAll(),
        ]);
    }

    #[IsGranted('ROLE_ADMIN')]
    #[Route('/gestion/addformmachine', name: 'gestion_machine_addform')]
    public function addFormMachine(ManagerRegistry $doctrine, Request $request): Response
    {
        $machine = new Machine();
        $form = $this->createForm(MachineType::class, $machine);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $user = $this->getUser(); // Get the logged-in user
            $machine->setUser($user); // Assuming Machine entity has a setUser method
            $em = $doctrine->getManager();
            $em->persist($machine);
            $em->flush();
            return $this->redirectToRoute('gestion_machine_show');
        }

        return $this->render('gestion_machine/addformmachine.html.twig', [
            'formadd' => $form->createView(),
        ]);
    }
    #[IsGranted('ROLE_ADMIN')]
    #[Route('/gestion/updatemachine/{id}', name: 'gestion_machine_update')]
    public function updateMachine(ManagerRegistry $doctrine, Request $request, Machine $machine): Response
    {
        $form = $this->createForm(MachineType::class, $machine);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $doctrine->getManager()->flush();
            return $this->redirectToRoute('gestion_machine_show');
        }

        return $this->render('gestion_machine/addformmachine.html.twig', [
            'formadd' => $form->createView(),
        ]);
    }


    #[IsGranted('ROLE_ADMIN')]
    #[Route('/gestion/deletemachine/{id}', name: 'gestion_machine_delete')]
    public function deleteMachine(ManagerRegistry $doctrine, Machine $machine): Response
    {
        $em = $doctrine->getManager();
        $em->remove($machine);
        $em->flush();
        return $this->redirectToRoute('gestion_machine_show');
    }


    #[IsGranted('ROLE_AGRICULTEUR')]
    #[Route('/gestion/showmachinesfront', name: 'gestion_machine_show_front')]
    public function showMachinesfront(MachineRepository $repo): Response
    {
        return $this->render('gestion_machine/show_machine_front.html.twig', [
            'tabmachines' => $repo->findAll(),
        ]);
    }

    #[Route('/gestion/showmachinesfront2', name: 'gestion_machine_show_front2')]
    public function showMachinesfront2(MachineRepository $repo): Response
    {
        return $this->render('gestion_machine/show_machine_front2.html.twig', [
            'tabmachines' => $repo->findBy(['disponibilite' => 'oui']),
        ]);
    }

    #[IsGranted('ROLE_AGRICULTEUR')]
    #[Route('/gestion/addformmachinefront', name: 'gestion_machine_addformfront')]
    public function addFormMachinefront(ManagerRegistry $doctrine, Request $request): Response
    {
        $machine = new Machine();
        $form = $this->createForm(MachineType::class, $machine);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $user = $this->getUser(); // Get the logged-in user
            $machine->setUser($user); // Assuming Machine entity has a setUser method
            $em = $doctrine->getManager();
            $em->persist($machine);
            $em->flush();
            return $this->redirectToRoute('gestion_machine_show_front');
        }

        return $this->render('gestion_machine/addformmachinefront.html.twig', [
            'formadd' => $form->createView(),
        ]);
    }
    #[IsGranted('ROLE_AGRICULTEUR')]
    #[Route('/gestion/updatemachinefront/{id}', name: 'gestion_machine_updatefront')]
    public function updateMachinefront(ManagerRegistry $doctrine, Request $request, Machine $machine): Response
    {
        $form = $this->createForm(MachineType::class, $machine);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $doctrine->getManager()->flush();
            return $this->redirectToRoute('gestion_machine_show_front');
        }

        return $this->render('gestion_machine/addformmachinefront.html.twig', [
            'formadd' => $form->createView(),
        ]);
    }
    #[IsGranted('ROLE_AGRICULTEUR')]
    #[Route('/gestion/deletemachinefront/{id}', name: 'gestion_machine_deletefront')]
    public function deleteMachinefront(ManagerRegistry $doctrine, Machine $machine): Response
    {
        $em = $doctrine->getManager();
        $em->remove($machine);
        $em->flush();
        return $this->redirectToRoute('gestion_machine_show_front');
    }

    #[Route('/gestion/materiel', name: 'gestion_materiel')]
    public function gestionMateriel(): Response
    {
        return $this->render('gestion_machine/initiale_front.html.twig');
    }

    #[Route('/gestion/machine/{id}', name: 'gestion_machine_details')]
    public function show(Machine $machine): Response
    {
        return $this->render('machine/show.html.twig', [
            'machine' => $machine,
        ]);
    }
}
