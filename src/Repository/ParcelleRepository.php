<?php

namespace App\Repository;

use App\Entity\Parcelle;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

/**
 * @extends ServiceEntityRepository<Parcelle>
 */
class ParcelleRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Parcelle::class);
    }

    //    /**
    //     * @return Parcelle[] Returns an array of Parcelle objects
    //     */
    //    public function findByExampleField($value): array
    //    {
    //        return $this->createQueryBuilder('p')
    //            ->andWhere('p.exampleField = :val')
    //            ->setParameter('val', $value)
    //            ->orderBy('p.id', 'ASC')
    //            ->setMaxResults(10)
    //            ->getQuery()
    //            ->getResult()
    //        ;
    //    }

    //    public function findOneBySomeField($value): ?Parcelle
    //    {
    //        return $this->createQueryBuilder('p')
    //            ->andWhere('p.exampleField = :val')
    //            ->setParameter('val', $value)
    //            ->getQuery()
    //            ->getOneOrNullResult()
    //        ;
    //    }

    public function findBySearchCriteria(array $criteria, string $sort = 'superficie', string $direction = 'ASC')
    {
        $queryBuilder = $this->createQueryBuilder('p');
    
        // Filtrer par nom si un nom est fourni
        if (!empty($criteria['nom'])) {
            $queryBuilder->andWhere('p.nom LIKE :nom')
                ->setParameter('nom', '%' . $criteria['nom'] . '%');
        }
    
        // Filtrer par type de sol si un type est sélectionné
        if (!empty($criteria['typeSol'])) {
            $queryBuilder->andWhere('p.typeSol = :typeSol')
                ->setParameter('typeSol', $criteria['typeSol']);
        }
    
        // Trier par le champ spécifié (par défaut : superficie)
        $queryBuilder->orderBy('p.' . $sort, $direction);
    
        return $queryBuilder->getQuery()->getResult();
    }


    public function findBySearchCriteriaQuery(array $criteria, string $sort = 'superficie', string $direction = 'ASC')
{
    $queryBuilder = $this->createQueryBuilder('p');

    // Filtrer par nom si un nom est fourni
    if (!empty($criteria['nom'])) {
        $queryBuilder->andWhere('p.nom LIKE :nom')
            ->setParameter('nom', '%' . $criteria['nom'] . '%');
    }

    // Filtrer par type de sol si un type est sélectionné
    if (!empty($criteria['typeSol'])) {
        $queryBuilder->andWhere('p.typeSol = :typeSol')
            ->setParameter('typeSol', $criteria['typeSol']);
    }

    // Trier par le champ spécifié (par défaut : superficie)
    $queryBuilder->orderBy('p.' . $sort, $direction);

    return $queryBuilder->getQuery();
}
}
