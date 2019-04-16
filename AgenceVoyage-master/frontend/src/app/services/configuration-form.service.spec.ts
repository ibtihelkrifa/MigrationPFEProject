import { TestBed } from '@angular/core/testing';

import { ConfigurationFormService } from './configuration-form.service';

describe('ConfigurationFormService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: ConfigurationFormService = TestBed.get(ConfigurationFormService);
    expect(service).toBeTruthy();
  });
});
