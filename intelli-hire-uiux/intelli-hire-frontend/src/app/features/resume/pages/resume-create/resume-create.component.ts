import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';
import { ResumeService } from '../../services/resume.service';
import { LoadingSpinnerComponent } from '../../../../shared/components/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-resume-create',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    LoadingSpinnerComponent,
    TranslatePipe
  ],
  templateUrl: './resume-create.component.html',
  styleUrl: './resume-create.component.scss'
})
export class ResumeCreateComponent {
  form: FormGroup;
  loading = false;

  constructor(
    private fb: FormBuilder,
    private resumeService: ResumeService,
    public router: Router
  ) {
    this.form = this.fb.group({
      title: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(200)]]
    });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading = true;
    this.resumeService.create(this.form.value).subscribe({
      next: resume => this.router.navigate(['/resumes', resume.id]),
      error: () => { this.loading = false; }
    });
  }

  get title() { return this.form.get('title')!; }
}
