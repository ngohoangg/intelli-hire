import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { TranslatePipe } from '@ngx-translate/core';
import { ResumeExperience, ResumeExperienceRequest } from '../../../../core/models/experience.model';

export interface ExperienceFormData {
  resumeId: string;
  experience?: ResumeExperience;
}

function dateRangeValidator(group: AbstractControl): ValidationErrors | null {
  const start = group.get('startDate')?.value;
  const end = group.get('endDate')?.value;
  const isCurrent = group.get('isCurrent')?.value;
  if (isCurrent || !start || !end) return null;
  return new Date(start) <= new Date(end) ? null : { dateRange: true };
}

@Component({
  selector: 'app-experience-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatCheckboxModule,
    TranslatePipe
  ],
  templateUrl: './experience-form.component.html',
  styleUrl: './experience-form.component.scss'
})
export class ExperienceFormComponent {
  form: FormGroup;
  isEdit: boolean;

  constructor(
    public dialogRef: MatDialogRef<ExperienceFormComponent, ResumeExperienceRequest | null>,
    @Inject(MAT_DIALOG_DATA) public data: ExperienceFormData,
    private fb: FormBuilder
  ) {
    this.isEdit = !!data.experience;
    const exp = data.experience;

    this.form = this.fb.group({
      company: [exp?.company ?? '', [Validators.required, Validators.maxLength(200)]],
      title: [exp?.title ?? '', [Validators.required, Validators.maxLength(200)]],
      startDate: [exp?.start_date ? new Date(exp.start_date) : null, Validators.required],
      endDate: [exp?.end_date ? new Date(exp.end_date) : null],
      description: [exp?.description ?? '', Validators.maxLength(2000)],
      isCurrent: [exp?.is_current ?? false]
    }, { validators: dateRangeValidator });

    this.form.get('isCurrent')?.valueChanges.subscribe((isCurrent: boolean) => {
      const endDateCtrl = this.form.get('endDate')!;
      if (isCurrent) {
        endDateCtrl.setValue(null);
        endDateCtrl.disable();
      } else {
        endDateCtrl.enable();
      }
    });

    if (exp?.is_current) {
      this.form.get('endDate')?.disable();
    }
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const val = this.form.getRawValue();
    const result: ResumeExperienceRequest = {
      company: val.company,
      title: val.title,
      start_date: this.formatDate(val.startDate),
      end_date: val.isCurrent ? null : (val.endDate ? this.formatDate(val.endDate) : null),
      description: val.description || undefined,
      is_current: val.isCurrent
    };
    this.dialogRef.close(result);
  }

  cancel(): void {
    this.dialogRef.close(null);
  }

  private formatDate(date: Date): string {
    return date.toISOString().split('T')[0];
  }

  get company() { return this.form.get('company')!; }
  get title() { return this.form.get('title')!; }
  get startDate() { return this.form.get('startDate')!; }
  get endDate() { return this.form.get('endDate')!; }
  get isCurrent() { return this.form.get('isCurrent')!; }
  get hasDateRangeError() { return this.form.hasError('dateRange') && this.startDate.touched && this.endDate.touched; }
}
