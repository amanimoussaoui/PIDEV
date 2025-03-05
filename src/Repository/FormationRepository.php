<?php

namespace App\Repository;

use App\Entity\Formation;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

/**
 * @extends ServiceEntityRepository<Formation>
 */
class FormationRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Formation::class);
    }

    //    /**
    //     * @return Formation[] Returns an array of Formation objects
    //     */
    //    public function findByExampleField($value): array
    //    {
    //        return $this->createQueryBuilder('f')
    //            ->andWhere('f.exampleField = :val')
    //            ->setParameter('val', $value)
    //            ->orderBy('f.id', 'ASC')
    //            ->setMaxResults(10)
    //            ->getQuery()
    //            ->getResult()
    //        ;
    //    }

    //    public function findOneBySomeField($value): ?Formation
    //    {
    //        return $this->createQueryBuilder('f')
    //            ->andWhere('f.exampleField = :val')
    //            ->setParameter('val', $value)
    //            ->getQuery()
    //            ->getOneOrNullResult()
    //        ;
    //    }
    public function save(Formation $formation, bool $flush = false): void
    {
        $entityManager = $this->getEntityManager();

        // Persiste l'entité (prépare l'ajout en base de données)
        $entityManager->persist($formation);

        if ($flush) {
            // Exécute la requête en base de données
            $entityManager->flush();
        }
    }

    public function remove(Formation $formation, bool $flush = false): void
    {
        $entityManager = $this->getEntityManager();

        // Supprime l'entité Formation
        $entityManager->remove($formation);

        if ($flush) {
            // Exécute la suppression en base de données
            $entityManager->flush();
        }
    }

}
