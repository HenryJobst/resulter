import type { Certificate } from '@/features/certificate/model/certificate'
import { GenericService } from '@/features/generic/services/GenericService'

const certificateUrl: string = '/event_certificate'

export class CertificateService extends GenericService<Certificate> {
    constructor() {
        super(certificateUrl)
    }
}

export const certificateService = new CertificateService()
