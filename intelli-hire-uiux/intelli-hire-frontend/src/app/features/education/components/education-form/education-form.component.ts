import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { TranslatePipe } from '@ngx-translate/core';
import { ResumeEducation, ResumeEducationRequest } from '../../../../core/models/education.model';

export interface EducationFormData {
  resumeId: string;
  education?: ResumeEducation;
}

const currentYear = new Date().getFullYear();

function yearRangeValidator(control: AbstractControl): ValidationErrors | null {
  const val = control.value;
  if (val === null || val === '' || val === undefined) return null;
  const n = Number(val);
  if (isNaN(n)) return { yearInvalid: true };
  if (n < 1900 || n > currentYear) return { yearRange: true };
  return null;
}

function yearOrderValidator(group: AbstractControl): ValidationErrors | null {
  const start = group.get('startYear')?.value;
  const end = group.get('endYear')?.value;
  if (!start || !end) return null;
  return Number(start) <= Number(end) ? null : { yearOrder: true };
}

@Component({
  selector: 'app-education-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    TranslatePipe
  ],
  templateUrl: './education-form.component.html',
  styleUrl: './education-form.component.scss'
})
export class EducationFormComponent {
  form: FormGroup;
  isEdit: boolean;
  currentYear = currentYear;

  constructor(
    public dialogRef: MatDialogRef<EducationFormComponent, ResumeEducationRequest | null>,
    @Inject(MAT_DIALOG_DATA) public data: EducationFormData,
    private fb: FormBuilder
  ) {
    this.isEdit = !!data.education;
    const edu = data.education;

    this.form = this.fb.group({
      school: [edu?.school ?? '', [Validators.required, Validators.maxLength(300)]],
      degree: [edu?.degree ?? '', Validators.maxLength(200)],
      field: [edu?.field ?? '', Validators.maxLength(200)],
      startYear: [edu?.start_year ?? null, yearRangeValidator],
      endYear: [edu?.end_year ?? null, yearRangeValidator],
      description: [edu?.description ?? '', Validators.maxLength(2000)]
    }, { validators: yearOrderValidator });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const val = this.form.value;
    const result: ResumeEducationRequest = {
      school: val.school,
      degree: val.degree || undefined,
      field: val.field || undefined,
      start_year: val.startYear ? Number(val.startYear) : null,
      end_year: val.endYear ? Number(val.endYear) : null,
      description: val.description || undefined
    };
    this.dialogRef.close(result);
  }

  cancel(): void {
    this.dialogRef.close(null);
  }

  get school() { return this.form.get('school')!; }
  get startYear() { return this.form.get('startYear')!; }
  get endYear() { return this.form.get('endYear')!; }
  get hasYearOrderError() { return this.form.hasError('yearOrder') && this.startYear.touched && this.endYear.touched; }
}
