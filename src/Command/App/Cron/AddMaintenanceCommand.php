<?php

namespace App\Cron;

use App\Repository\MachineRepository;
use App\Entity\MaintenanceHistorique;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Component\Console\Attribute\AsCommand;
use Symfony\Component\Console\Command\Command;
use Symfony\Component\Console\Input\InputInterface;
use Symfony\Component\Console\Output\OutputInterface;

#[AsCommand(
    name: 'app:add-maintenance-history',
    description: 'Ajoute automatiquement les maintenances dont la date est arrivée.',
)]
class AddMaintenanceCommand extends Command
{
    private MachineRepository $machineRepository;
    private EntityManagerInterface $entityManager;

    public function __construct(MachineRepository $machineRepository, EntityManagerInterface $entityManager)
    {
        parent::__construct();
        $this->machineRepository = $machineRepository;
        $this->entityManager = $entityManager;
    }

    protected function execute(InputInterface $input, OutputInterface $output): int
    {
        $machines = $this->machineRepository->findMachinesForMaintenance();

        if (empty($machines)) {
            $output->writeln('Aucune machine à ajouter à l\'historique.');
            return Command::SUCCESS;
        }

        foreach ($machines as $machine) {
            $historique = new MaintenanceHistorique();
            $historique->setMachine($machine);
            $historique->setDateMaintenance($machine->getDateMaintenance());

            $this->entityManager->persist($historique);
        }

        $this->entityManager->flush();

        $output->writeln('Historique de maintenance mis à jour avec succès.');
        return Command::SUCCESS;
    }
}
