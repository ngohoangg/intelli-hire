import { Component, Input, OnInit } from '@angular/core';
import { CommonModule, DecimalPipe } from '@angular/common';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { forkJoin } from 'rxjs';
import { ResumeSkillService } from '../../services/resume-skill.service';
import { SkillService } from '../../../skills/services/skill.service';
import { ResumeSkill, Skill } from '../../../../core/models/skill.model';
import { AddSkillDialogComponent, AddSkillDialogData } from '../add-skill-dialog/add-skill-dialog.component';
import { ConfirmDialogComponent } from '../../../../shared/components/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-resume-skill-list',
  standalone: true,
  imports: [
    CommonModule,
    DecimalPipe,
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule
  ],
  templateUrl: './resume-skill-list.component.html',
  styleUrl: './resume-skill-list.component.scss'
})
export class ResumeSkillListComponent implements OnInit {
  @Input({ required: true }) resumeId!: string;

  skills: ResumeSkill[] = [];
  skillMap: Record<string, Skill> = {};
  loading = false;

  constructor(
    private skillService: ResumeSkillService,
    private catalogService: SkillService,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    forkJoin({
      resumeSkills: this.skillService.list(this.resumeId),
      catalog: this.catalogService.list()
    }).subscribe({
      next: ({ resumeSkills, catalog }) => {
        this.skills = resumeSkills;
        this.skillMap = catalog.reduce((map, s) => ({ ...map, [s.id]: s }), {});
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  skillName(skill: ResumeSkill): string {
    return skill.skill_name ?? this.skillMap[skill.skill_id]?.name ?? skill.skill_id;
  }

  skillCategory(skill: ResumeSkill): string {
    return this.skillMap[skill.skill_id]?.category ?? '';
  }

  openAdd(): void {
    const data: AddSkillDialogData = { resumeId: this.resumeId };
    this.dialog.open(AddSkillDialogComponent, { data, width: '560px' })
      .afterClosed().subscribe(result => {
        if (result) {
          this.skillService.create(this.resumeId, result).subscribe(() => this.load());
        }
      });
  }

  openEdit(skill: ResumeSkill): void {
    const catalogSkill = this.skillMap[skill.skill_id];
    const data: AddSkillDialogData = { resumeId: this.resumeId, existing: skill, existingSkillInfo: catalogSkill };
    this.dialog.open(AddSkillDialogComponent, { data, width: '560px' })
      .afterClosed().subscribe(result => {
        if (result) {
          this.skillService.update(this.resumeId, skill.id, result).subscribe(() => this.load());
        }
      });
  }

  delete(skill: ResumeSkill): void {
    const name = this.skillName(skill);
    this.dialog.open(ConfirmDialogComponent, {
      data: { title: 'Remove Skill', message: `Remove "${name}" from this resume?` }
    }).afterClosed().subscribe(confirmed => {
      if (confirmed) {
        this.skillService.delete(this.resumeId, skill.id).subscribe(() => this.load());
      }
    });
  }


  private expandedSkills = new Set<string>();
  readonly EVIDENCE_THRESHOLD = 120;

  isLongEvidence(text: string | undefined): boolean {
    return !!text && text.length > this.EVIDENCE_THRESHOLD;
  }

  isExpanded(skillId: string): boolean {
    return this.expandedSkills.has(skillId);
  }

  toggleExpand(skillId: string): void {
    if (this.expandedSkills.has(skillId)) {
      this.expandedSkills.delete(skillId);
    } else {
      this.expandedSkills.add(skillId);
    }
  }

  evidencePreview(text: string): string {
    return text.substring(0, this.EVIDENCE_THRESHOLD) + '…';
  }

  get primarySkills(): ResumeSkill[] {
    return this.skills.filter(s => s.is_primary);
  }

  get otherSkills(): ResumeSkill[] {
    return this.skills.filter(s => !s.is_primary);
  }
}
