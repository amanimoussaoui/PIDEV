<?php

namespace App\Repository;

use App\Entity\Terrain;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

/**
 * @extends ServiceEntityRepository<Terrain>
 */
class TerrainRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Terrain::class);
    }

    //    /**
    //     * @return Terrain[] Returns an array of Terrain objects
    //     */
    //    public function findByExampleField($value): array
    //    {
    //        return $this->createQueryBuilder('t')
    //            ->andWhere('t.exampleField = :val')
    //            ->setParameter('val', $value)
    //            ->orderBy('t.id', 'ASC')
    //            ->setMaxResults(10)
    //            ->getQuery()
    //            ->getResult()
    //        ;
    //    }

    //    public function findOneBySomeField($value): ?Terrain
    //    {
    //        return $this->createQueryBuilder('t')
    //            ->andWhere('t.exampleField = :val')
    //            ->setParameter('val', $value)
    //            ->getQuery()
    //            ->getOneOrNullResult()
    //        ;
    //    } 
    
    
     /**
     * Recherche des terrains selon la localisation
     */
    public function findByLocalisation(string $localisation)
    {
        return $this->createQueryBuilder('t')
            ->where('t.localisation LIKE :localisation')
            ->setParameter('localisation', '%' . $localisation . '%')
            ->getQuery()
            ->getResult();
    }

   



    public function findTopTerrainsWithMostCandidatures(int $limit): array
    {
        return $this->createQueryBuilder('t')
            ->leftJoin('t.candidatures', 'c') // Jointure avec la table des candidatures
            ->groupBy('t.id') // Grouper par l'ID du terrain
            ->select('t, COUNT(c.id) as candidatureCount') // Sélectionner le nombre de candidatures
            ->orderBy('candidatureCount', 'DESC') // Trier par le nombre de candidatures (du plus grand au plus petit)
            ->setMaxResults($limit) // Limiter le nombre de résultats
            ->getQuery()
            ->getResult();
    }
    

}
