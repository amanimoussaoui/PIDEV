<?php

namespace App\Repository;

use App\Entity\Parcelle;
use App\Entity\Utilisateurs;
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

  

    public function findBySearchCriteria(array $criteria, string $sort = 'superficie', string $direction = 'ASC', ?Utilisateurs $user = null): array
    {
        $qb = $this->createQueryBuilder('p');
    
        // Add search criteria
        if (!empty($criteria['nom'])) {
            $qb->andWhere('p.nom LIKE :nom')
               ->setParameter('nom', '%' . $criteria['nom'] . '%');
        }
    
        if (!empty($criteria['localisation'])) {
            $qb->andWhere('p.localisation LIKE :localisation')
               ->setParameter('localisation', '%' . $criteria['localisation'] . '%');
        }
    
        if (!empty($criteria['typeSol'])) {
            $qb->andWhere('p.typeSol = :typeSol')
               ->setParameter('typeSol', $criteria['typeSol']);
        }
    
        // Filter by the logged-in user
        if ($user) {
            $qb->andWhere('p.utilisateur = :user')
               ->setParameter('user', $user);
        }
    
        // Add sorting
        $qb->orderBy('p.' . $sort, $direction);
    
        return $qb->getQuery()->getResult();
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
    
        return $queryBuilder->getQuery()->getResult();
    }
}
