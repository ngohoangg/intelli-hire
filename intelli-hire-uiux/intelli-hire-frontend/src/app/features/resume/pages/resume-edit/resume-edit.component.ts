import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { TranslatePipe } from '@ngx-translate/core';
import { ResumeService } from '../../services/resume.service';
import { LoadingSpinnerComponent } from '../../../../shared/components/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-resume-edit',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    LoadingSpinnerComponent,
    TranslatePipe
  ],
  templateUrl: './resume-edit.component.html',
  styleUrl: './resume-edit.component.scss'
})
export class ResumeEditComponent implements OnInit {
  form: FormGroup;
  loading = false;
  resumeId!: string;

  constructor(
    private fb: FormBuilder,
    private resumeService: ResumeService,
    private route: ActivatedRoute,
    public router: Router
  ) {
    this.form = this.fb.group({
      title: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(200)]]
    });
  }

  ngOnInit(): void {
    this.resumeId = this.route.snapshot.paramMap.get('id')!;
    this.loading = true;
    this.resumeService.getById(this.resumeId).subscribe({
      next: resume => {
        this.form.patchValue({ title: resume.title });
        this.loading = false;
      },
      error: () => { this.loading = false; this.router.navigate(['/resumes']); }
    });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading = true;
    this.resumeService.update(this.resumeId, this.form.value).subscribe({
      next: () => this.router.navigate(['/resumes', this.resumeId]),
      error: () => { this.loading = false; }
    });
  }

  get title() { return this.form.get('title')!; }
}
