import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormControl } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatIconModule } from '@angular/material/icon';
import { Subject, of } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap, catchError, takeUntil } from 'rxjs/operators';
import { SkillService } from '../../services/skill.service';
import { Skill } from '../../../../core/models/skill.model';

@Component({
  selector: 'app-skill-search',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatAutocompleteModule,
    MatIconModule
  ],
  templateUrl: './skill-search.component.html',
  styleUrl: './skill-search.component.scss'
})
export class SkillSearchComponent implements OnInit, OnDestroy {
  @Input() placeholder = 'Tìm kỹ năng...';
  @Input() label = 'Kỹ năng';
  @Output() skillSelected = new EventEmitter<Skill>();
  @Output() cleared = new EventEmitter<void>();

  searchControl = new FormControl('');
  results: Skill[] = [];
  searching = false;

  private destroy$ = new Subject<void>();

  constructor(private skillService: SkillService) {}

  ngOnInit(): void {
    this.searchControl.valueChanges.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      switchMap(q => {
        if (!q || typeof q !== 'string' || q.trim().length === 0) {
          this.results = [];
          return of([]);
        }
        this.searching = true;
        return this.skillService.search(q.trim()).pipe(catchError(() => of([])));
      }),
      takeUntil(this.destroy$)
    ).subscribe(skills => {
      this.results = skills;
      this.searching = false;
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  onOptionSelected(skill: Skill): void {
    this.skillSelected.emit(skill);
  }

  displayFn(skill: Skill | string | null): string {
    if (!skill) return '';
    if (typeof skill === 'string') return skill;
    return skill.name;
  }

  clear(): void {
    this.searchControl.setValue('');
    this.results = [];
    this.cleared.emit();
  }
}
