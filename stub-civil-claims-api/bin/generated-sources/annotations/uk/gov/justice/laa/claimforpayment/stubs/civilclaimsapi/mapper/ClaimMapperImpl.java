package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.mapper;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.entity.ClaimEntity;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.Claim;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-01-29T12:22:15+0000",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.45.0.v20260101-2150, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class ClaimMapperImpl implements ClaimMapper {

    @Override
    public Claim toClaim(ClaimEntity claimEntity) {
        if ( claimEntity == null ) {
            return null;
        }

        Claim.ClaimBuilder claim = Claim.builder();

        claim.category( claimEntity.getCategory() );
        claim.claimed( claimEntity.getClaimed() );
        claim.client( claimEntity.getClient() );
        claim.concluded( claimEntity.getConcluded() );
        claim.feeType( claimEntity.getFeeType() );
        claim.id( claimEntity.getId() );
        claim.providerUserId( claimEntity.getProviderUserId() );
        claim.submissionId( claimEntity.getSubmissionId() );
        claim.ufn( claimEntity.getUfn() );

        return claim.build();
    }

    @Override
    public ClaimEntity toClaimEntity(Claim claim) {
        if ( claim == null ) {
            return null;
        }

        ClaimEntity.ClaimEntityBuilder claimEntity = ClaimEntity.builder();

        claimEntity.category( claim.getCategory() );
        claimEntity.claimed( claim.getClaimed() );
        claimEntity.client( claim.getClient() );
        claimEntity.concluded( claim.getConcluded() );
        claimEntity.feeType( claim.getFeeType() );
        claimEntity.id( claim.getId() );
        claimEntity.providerUserId( claim.getProviderUserId() );
        claimEntity.submissionId( claim.getSubmissionId() );
        claimEntity.ufn( claim.getUfn() );

        return claimEntity.build();
    }
}
