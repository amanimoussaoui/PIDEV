<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: Please modify to your needs!
 */
final class Version20250216120002 extends AbstractMigration
{
    public function getDescription(): string
    {
        return '';
    }

    public function up(Schema $schema): void
    {
        // this up() migration is auto-generated, please modify it to your needs
        $this->addSql('ALTER TABLE activite DROP FOREIGN KEY FK_B8755515B108249D');
        $this->addSql('DROP INDEX IDX_ACTIVITE_CULTURE ON activite');
        $this->addSql('ALTER TABLE culture DROP FOREIGN KEY FK_CULTURE_PARCELLE');
        $this->addSql('DROP INDEX IDX_CULTURE_PARCELLE ON culture');
        $this->addSql('ALTER TABLE parcelle ADD latitude DOUBLE PRECISION DEFAULT NULL, ADD longitude DOUBLE PRECISION DEFAULT NULL');
        $this->addSql('ALTER TABLE recolte DROP FOREIGN KEY FK_3433713CB108249D');
        $this->addSql('DROP INDEX IDX_RECOLTE_CULTURE ON recolte');
    }

    public function down(Schema $schema): void
    {
        // this down() migration is auto-generated, please modify it to your needs
        $this->addSql('CREATE INDEX IDX_ACTIVITE_CULTURE ON activite (culture_id)');
        $this->addSql('CREATE INDEX IDX_CULTURE_PARCELLE ON culture (parcelle_id)');
        $this->addSql('ALTER TABLE parcelle DROP latitude, DROP longitude');
        $this->addSql('CREATE INDEX IDX_RECOLTE_CULTURE ON recolte (culture_id)');
    }
}
