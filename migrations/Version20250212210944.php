<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: Please modify to your needs!
 */
final class Version20250212210944 extends AbstractMigration
{
    public function getDescription(): string
    {
        return '';
    }

    public function up(Schema $schema): void
    {
        // this up() migration is auto-generated, please modify it to your needs
        $this->addSql('ALTER TABLE activite DROP FOREIGN KEY FK_B8755515B108249D');
        $this->addSql('ALTER TABLE activite ADD CONSTRAINT FK_B8755515B108249D FOREIGN KEY (culture_id) REFERENCES culture (id)');
        $this->addSql('ALTER TABLE culture DROP FOREIGN KEY FK_B6A99CEB4433ED66');
        $this->addSql('ALTER TABLE culture ADD CONSTRAINT FK_B6A99CEB4433ED66 FOREIGN KEY (parcelle_id) REFERENCES parcelle (id)');
        $this->addSql('ALTER TABLE recolte DROP FOREIGN KEY FK_3433713CB108249D');
        $this->addSql('ALTER TABLE recolte ADD CONSTRAINT FK_3433713CB108249D FOREIGN KEY (culture_id) REFERENCES culture (id)');
    }

    public function down(Schema $schema): void
    {
        // this down() migration is auto-generated, please modify it to your needs
        $this->addSql('ALTER TABLE activite DROP FOREIGN KEY FK_B8755515B108249D');
        $this->addSql('ALTER TABLE activite ADD CONSTRAINT FK_B8755515B108249D FOREIGN KEY (culture_id) REFERENCES culture (id) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE culture DROP FOREIGN KEY FK_B6A99CEB4433ED66');
        $this->addSql('ALTER TABLE culture ADD CONSTRAINT FK_B6A99CEB4433ED66 FOREIGN KEY (parcelle_id) REFERENCES parcelle (id) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE recolte DROP FOREIGN KEY FK_3433713CB108249D');
        $this->addSql('ALTER TABLE recolte ADD CONSTRAINT FK_3433713CB108249D FOREIGN KEY (culture_id) REFERENCES culture (id) ON DELETE CASCADE');
    }
}
