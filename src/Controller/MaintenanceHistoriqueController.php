<?php

namespace App\Controller;

use App\Entity\MaintenanceHistorique;
use App\Entity\Machine;
use App\Repository\MaintenanceHistoriqueRepository;
use App\Repository\MachineRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

#[Route('/historique-maintenance')]
class MaintenanceHistoriqueController extends AbstractController
{
    #[Route('/', name: 'app_maintenance_historique_index', methods: ['GET'])]
    public function index(MaintenanceHistoriqueRepository $maintenanceHistoriqueRepository): Response
    {
        $historiqueMaintenances = $maintenanceHistoriqueRepository->findAll();

        return $this->render('maintenance_historique/historique_maintenance.html.twig', [
            'historiqueMaintenances' => $historiqueMaintenances,
        ]);
    }

    #[Route('/check', name: 'app_maintenance_historique_check', methods: ['GET'])]
    public function checkAndAddMaintenance(
        MachineRepository $machineRepository, 
        MaintenanceHistoriqueRepository $maintenanceHistoriqueRepository, 
        EntityManagerInterface $entityManager
    ): Response {
        $today = new \DateTime(); // Date actuelle
        $machines = $machineRepository->findAll(); // Récupérer toutes les machines

        foreach ($machines as $machine) {
            if ($machine->getDateMaintenance() && $machine->getDateMaintenance()->format('Y-m-d') === $today->format('Y-m-d')) {
                // Vérifier si la maintenance existe déjà dans l'historique
                $existingMaintenance = $maintenanceHistoriqueRepository->findOneBy([
                    'machine' => $machine,
                    'dateMaintenance' => $today
                ]);

                if (!$existingMaintenance) {
                    $historique = new MaintenanceHistorique();
                    $historique->setMachine($machine);
                    $historique->setDateMaintenance($today);

                    $entityManager->persist($historique);
                    $entityManager->flush();
                }
            }
        }

        return new Response('Historique de maintenance mis à jour.');
    }
}
