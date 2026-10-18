import { Component, Inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { Subject, of } from 'rxjs';
import { debounceTime, switchMap, distinctUntilChanged, takeUntil, catchError } from 'rxjs/operators';
import { SkillService } from '../../../skills/services/skill.service';
import { Skill, ResumeSkill, ProficiencyLevel } from '../../../../core/models/skill.model';

export interface AddSkillDialogData {
  resumeId: string;
  existing?: ResumeSkill;
  existingSkillInfo?: Skill;
}

@Component({
  selector: 'app-add-skill-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
    MatAutocompleteModule,
    MatCheckboxModule
  ],
  templateUrl: './add-skill-dialog.component.html',
  styleUrl: './add-skill-dialog.component.scss'
})
export class AddSkillDialogComponent implements OnInit, OnDestroy {
  form: FormGroup;
  filteredSkills: Skill[] = [];
  selectedSkill: Skill | null = null;
  searching = false;
  readonly proficiencyLevels = Object.values(ProficiencyLevel);

  proficiencyLabel(level: string): string {
    const map: Record<string, string> = {
      BEGINNER: 'Cơ bản',
      INTERMEDIATE: 'Trung bình',
      ADVANCED: 'Nâng cao',
      EXPERT: 'Chuyên gia'
    };
    return map[level] ?? level;
  }

  private searchInput$ = new Subject<string>();
  private destroy$ = new Subject<void>();

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<AddSkillDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: AddSkillDialogData,
    private skillService: SkillService
  ) {
    const ex = data.existing;
    this.form = this.fb.group({
      skillSearch: [data.existingSkillInfo?.name ?? '', Validators.required],
      proficiency_level: [ex ? ex.proficiency_level : ProficiencyLevel.INTERMEDIATE, Validators.required],
      years_experience: [ex ? ex.years_experience : 1, [Validators.required, Validators.min(0), Validators.max(50)]],
      is_primary: [ex ? ex.is_primary : false]
    });
  }

  ngOnInit(): void {
    if (this.data.existing && this.data.existingSkillInfo) {
      this.selectedSkill = this.data.existingSkillInfo;
    }

    this.searchInput$.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      switchMap(q => {
        if (!q || q.length < 1) return of([]);
        this.searching = true;
        return this.skillService.search(q).pipe(catchError(() => of([])));
      }),
      takeUntil(this.destroy$)
    ).subscribe(skills => {
      this.filteredSkills = skills;
      this.searching = false;
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  onSearchInput(value: string): void {
    if (this.selectedSkill && this.selectedSkill.name !== value) {
      this.selectedSkill = null;
    }
    this.searchInput$.next(value);
  }

  selectSkill(skill: Skill): void {
    this.selectedSkill = skill;
    this.form.patchValue({ skillSearch: skill.name });
    this.filteredSkills = [];
  }

  displayFn(skill: Skill | string): string {
    if (typeof skill === 'string') return skill;
    return skill?.name ?? '';
  }

  submit(): void {
    if (this.form.invalid || !this.selectedSkill) {
      this.form.markAllAsTouched();
      return;
    }
    const { proficiency_level, years_experience, is_primary } = this.form.value;
    this.dialogRef.close({
      skill_id: this.selectedSkill.id,
      proficiency_level,
      years_experience: Number(years_experience),
      is_primary
    });
  }

  cancel(): void {
    this.dialogRef.close();
  }

  get isEdit(): boolean {
    return !!this.data.existing;
  }
}
