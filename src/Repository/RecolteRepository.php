<?php

namespace App\Repository;

use App\Entity\Recolte;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

/**
 * @extends ServiceEntityRepository<Recolte>
 */
class RecolteRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Recolte::class);
    }
    public function findBySearchAndFilter(?string $searchTerm, ?string $qualiteFilter, string $sort, string $direction): array
    {
        $allowedSortFields = ['id', 'dateRecolte', 'quantite', 'qualite', 'culture.nomCulture', 'prixUnitaire']; // Added prixUnitaire
        if (!in_array($sort, $allowedSortFields, true)) {
            $sort = 'dateRecolte'; // Default sort
        }
    
        $qb = $this->createQueryBuilder('r')
            ->leftJoin('r.culture', 'c')
            ->addSelect('c');
    
        if ($searchTerm) {
            $qb->andWhere('c.nomCulture LIKE :searchTerm')
               ->setParameter('searchTerm', '%' . $searchTerm . '%');
        }
    
        if ($qualiteFilter) {
            $qb->andWhere('r.qualite = :qualiteFilter')
               ->setParameter('qualiteFilter', $qualiteFilter);
        }
    
        $qb->orderBy('r.' . $sort, $direction);
    
        return $qb->getQuery()->getResult();
    }
}