import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ProfileService } from '../../../../core/services/profile.service';
import { AuthService } from '../../../../core/services/auth.service';
import { LoadingSpinnerComponent } from '../../../../shared/components/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, LoadingSpinnerComponent],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.scss'
})
export class ProfileComponent implements OnInit {
  private destroyRef = inject(DestroyRef);

  form: FormGroup;
  loading = false;
  saving = false;
  success = '';
  error = '';

  constructor(private fb: FormBuilder, private profileService: ProfileService, public auth: AuthService) {
    this.form = this.fb.group({
      firstName: [''], middleName: [''], lastName: [''],
      phone: ['', Validators.pattern(/^\+?[0-9]+$/)], address: [''], birthOfDate: [''],
      gender: [''], githubUrl: [''], linkedInUrl: [''], avatarUrl: ['']
    });
  }

  ngOnInit(): void {
    this.loading = true;
    this.profileService.getMyProfile()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: p => {
          this.form.patchValue({
            firstName: p.first_name, middleName: p.middle_name, lastName: p.last_name,
            phone: p.phone, address: p.address, birthOfDate: p.birth_of_date,
            gender: p.gender ?? '', githubUrl: p.github_url, linkedInUrl: p.linked_in_url, avatarUrl: p.avatar_url
          });
          this.loading = false;
        },
        error: () => this.loading = false
      });
  }

  submit(): void {
    if (this.form.invalid) return;
    this.saving = true; this.success = ''; this.error = '';
    const v = this.form.value;
    const request = {
      first_name: v.firstName || null,
      middle_name: v.middleName || null,
      last_name: v.lastName || null,
      phone: v.phone || null,
      address: v.address || null,
      birth_of_date: v.birthOfDate || null,
      gender: v.gender || null,
      github_url: v.githubUrl || null,
      linked_in_url: v.linkedInUrl || null,
      avatar_url: v.avatarUrl || null,
    };
    this.profileService.updateMyProfile(request).subscribe({
      next: () => { this.success = 'Profile updated successfully.'; this.saving = false; },
      error: err => { this.error = err?.error?.message ?? 'Update failed'; this.saving = false; }
    });
  }
}
