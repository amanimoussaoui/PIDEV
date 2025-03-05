<?php

namespace App\Controller;

use App\Entity\Machine;
use App\Repository\MachineRepository;
use App\Form\MachineType;
use App\Repository\ReservationRepository;
use Doctrine\ORM\EntityManagerInterface;
use Doctrine\Persistence\ManagerRegistry;
use Knp\Component\Pager\PaginatorInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;
use Symfony\Component\Security\Http\Attribute\IsGranted;
use Dompdf\Dompdf;
use Dompdf\Options;

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
    #[IsGranted('ROLE_ADMIN')]
    #[Route('/machines', name: 'gestion_machines12')]
public function index2(Request $request, MachineRepository $machineRepository): Response
{
    $sort = $request->query->get('sort', null);

    if ($sort === 'name') {
        $machines = $machineRepository->findAllSortedByName();
    } else {
        $machines = $machineRepository->findAll();
    }

    return $this->render('gestion_machine\show_machines.html.twig', [
        'tabmachines' => $machines,
    ]);
}


#[Route('/machines2', name: 'gestion_machines2')]
public function showPaginated(
    Request $request, 
    MachineRepository $machineRepository, 
    PaginatorInterface $paginator
): Response {
    $user = $this->getUser();
    if (!$user) {
        throw $this->createAccessDeniedException('Vous devez être connecté pour voir les machines.');
    }

    // Création de la requête QueryBuilder pour afficher toutes les machines disponibles
    $queryBuilder = $machineRepository->createQueryBuilder('m')
        ->where('m.disponibilite = :disponibilite')
        ->setParameter('disponibilite', 'oui')
        ->orderBy('m.id', 'DESC');

    dump($queryBuilder->getQuery()->getSQL()); // Vérifier la requête SQL exécutée

    try {
        // Convertir QueryBuilder en Query
        $query = $queryBuilder->getQuery();

        // Paginer la requête
        $pagination = $paginator->paginate(
            $query,
            $request->query->getInt('page', 1), // Récupérer le numéro de page
            6 // Nombre d'éléments par page
        );

        // Vérifier si aucune machine n'est trouvée
        if (count($pagination) === 0) {
            $this->addFlash('warning', 'Aucune machine disponible trouvée.');
        }
    } catch (\Exception $e) {
        throw $this->createNotFoundException("Erreur lors de la récupération des machines.");
    }

    return $this->render('gestion_machine/show_machine_front2.html.twig', [
        'tabmachines' => $pagination, // On passe l'objet paginé à la vue
    ]);
}



#[Route('/machine/vote/{id}/{action}', name: 'machine_vote', methods: ['POST'])]
public function voteMachine(Request $request, Machine $machine, string $action, EntityManagerInterface $em): JsonResponse
{
    if ($action === 'like') {
        $machine->setLikes($machine->getLikes() + 1);
    } elseif ($action === 'dislike') {
        $machine->setDislikes($machine->getDislikes() + 1);
    }

    $em->persist($machine);
    $em->flush();

    return new JsonResponse([
        'success' => true,
        'likes' => $machine->getLikes(),
        'dislikes' => $machine->getDislikes(),
    ]);
}


#[IsGranted('ROLE_ADMIN')]
    #[Route('/statistiques-machines', name: 'statistiques-machines')]
    public function afficherStatistiquesMachines(
        MachineRepository $machineRepository, 
        ReservationRepository $reservationRepository
    ): Response {
        return $this->render('gestion_machine/show_machines.html.twig', [
            'machines' => $machineRepository->findAll(),
            'reservations' => $reservationRepository->findAll()
        ]);
    }
    #[Route('/gestion/machines/pdf', name: 'gestion_machines_pdf')]
    public function generatePdf(MachineRepository $machineRepository): Response
    {
        $machines = $machineRepository->findAll();
        $html = $this->renderView('gestion_machine/machines_pdf.html.twig', [
            'machines' => $machines
        ]);

        $pdfOptions = new Options();
        $pdfOptions->set('defaultFont', 'Arial');

        $dompdf = new Dompdf($pdfOptions);
        $dompdf->loadHtml($html);
        $dompdf->setPaper('A4', 'portrait');
        $dompdf->render();

        return new Response(
            $dompdf->output(),
            Response::HTTP_OK,
            [
                'Content-Type' => 'application/pdf',
                'Content-Disposition' => 'attachment; filename="machines.pdf"',
            ]
        );
    }
}