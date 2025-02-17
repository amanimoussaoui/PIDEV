<?php declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: Please modify to your needs!
 */
final class Version20250213234527 extends AbstractMigration
{
    public function getDescription(): string
    {
        return 'Fix primary key and foreign key constraints for activite, culture, and recolte tables.';
    }

    public function up(Schema $schema): void
    {
        // Fix activite table
        $this->addSql('ALTER TABLE activite CHANGE description description LONGTEXT NOT NULL, CHANGE type type VARCHAR(50) NOT NULL');
        
        // Use a unique name for the foreign key constraint
        $this->addSql('ALTER TABLE activite ADD CONSTRAINT FK_ACTIVITE_CULTURE FOREIGN KEY (culture_id) REFERENCES culture (id)');
        $this->addSql('CREATE INDEX IDX_ACTIVITE_CULTURE ON activite (culture_id)');

        // Fix culture table
        $this->addSql('ALTER TABLE culture CHANGE nom_culture nom_culture VARCHAR(100) NOT NULL, CHANGE statut statut VARCHAR(50) NOT NULL');
        $this->addSql('ALTER TABLE culture ADD CONSTRAINT FK_CULTURE_PARCELLE FOREIGN KEY (parcelle_id) REFERENCES parcelle (id)');
        $this->addSql('CREATE INDEX IDX_CULTURE_PARCELLE ON culture (parcelle_id)');

        // Fix recolte table
        $this->addSql('ALTER TABLE recolte CHANGE qualite qualite VARCHAR(50) NOT NULL');
        $this->addSql('ALTER TABLE recolte ADD CONSTRAINT FK_RECOLTE_CULTURE FOREIGN KEY (culture_id) REFERENCES culture (id)');
        $this->addSql('CREATE INDEX IDX_RECOLTE_CULTURE ON recolte (culture_id)');
    }

    public function down(Schema $schema): void
    {
        // Revert activite table
        $this->addSql('ALTER TABLE activite DROP FOREIGN KEY FK_ACTIVITE_CULTURE');
        $this->addSql('DROP INDEX IDX_ACTIVITE_CULTURE ON activite');
        $this->addSql('ALTER TABLE activite CHANGE description description LONGTEXT NOT NULL, CHANGE type type VARCHAR(50) NOT NULL');

        // Revert culture table
        $this->addSql('ALTER TABLE culture DROP FOREIGN KEY FK_CULTURE_PARCELLE');
        $this->addSql('DROP INDEX IDX_CULTURE_PARCELLE ON culture');
        $this->addSql('ALTER TABLE culture CHANGE nom_culture nom_culture VARCHAR(100) NOT NULL, CHANGE statut statut VARCHAR(50) NOT NULL');

        // Revert recolte table
        $this->addSql('ALTER TABLE recolte DROP FOREIGN KEY FK_RECOLTE_CULTURE');
        $this->addSql('DROP INDEX IDX_RECOLTE_CULTURE ON recolte');
        $this->addSql('ALTER TABLE recolte CHANGE qualite qualite VARCHAR(50) NOT NULL');
    }
}