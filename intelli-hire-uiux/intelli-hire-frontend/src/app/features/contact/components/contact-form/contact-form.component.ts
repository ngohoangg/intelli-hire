import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ResumeContactService } from '../../services/resume-contact.service';
import { ResumeContact } from '../../../../core/models/contact.model';

@Component({
  selector: 'app-contact-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatSnackBarModule
  ],
  templateUrl: './contact-form.component.html',
  styleUrl: './contact-form.component.scss'
})
export class ContactFormComponent implements OnInit {
  @Input({ required: true }) resumeId!: string;

  form: FormGroup;
  loading = false;
  saving = false;

  constructor(
    private fb: FormBuilder,
    private contactService: ResumeContactService,
    private snackBar: MatSnackBar
  ) {
    this.form = this.fb.group({
      fullName: ['', [Validators.required, Validators.maxLength(200)]],
      email: ['', [Validators.email, Validators.maxLength(200)]],
      phone: ['', [Validators.pattern(/^[+]?[\d\s\-]{7,15}$/)]],
      location: ['', Validators.maxLength(300)],
      linkedinUrl: ['', [Validators.pattern(/^(https?:\/\/)?(www\.)?linkedin\.com\/.*$/)]],
      otherInfo: ['', Validators.maxLength(1000)]
    });
  }

  ngOnInit(): void {
    this.loadContact();
  }

  private loadContact(): void {
    this.loading = true;
    this.contactService.getByResumeId(this.resumeId).subscribe({
      next: (contact: ResumeContact) => {
        this.form.patchValue({
          fullName: contact.full_name ?? '',
          email: contact.email ?? '',
          phone: contact.phone ?? '',
          location: contact.location ?? '',
          linkedinUrl: contact.linkedin_url ?? '',
          otherInfo: contact.other_info ?? ''
        });
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving = true;
    const val = this.form.value;
    this.contactService.upsert(this.resumeId, {
      full_name: val.fullName,
      email: val.email || undefined,
      phone: val.phone || undefined,
      location: val.location || undefined,
      linkedin_url: val.linkedinUrl || undefined,
      other_info: val.otherInfo || undefined
    }).subscribe({
      next: () => {
        this.saving = false;
        this.snackBar.open('Contact saved.', 'Close', { duration: 3000 });
      },
      error: () => { this.saving = false; }
    });
  }

  get fullName() { return this.form.get('fullName')!; }
  get email() { return this.form.get('email')!; }
  get phone() { return this.form.get('phone')!; }
  get linkedinUrl() { return this.form.get('linkedinUrl')!; }
}
