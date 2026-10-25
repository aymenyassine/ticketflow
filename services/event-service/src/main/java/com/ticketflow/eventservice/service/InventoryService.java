package com.ticketflow.eventservice.service;

import com.ticketflow.eventservice.dto.InventoryHoldRequest;
import com.ticketflow.eventservice.dto.InventoryHoldResponse;
import com.ticketflow.eventservice.entities.EventStatus;
import com.ticketflow.eventservice.entities.InventoryHold;
import com.ticketflow.eventservice.entities.InventoryHoldStatus;
import com.ticketflow.eventservice.entities.TicketTier;
import com.ticketflow.eventservice.exceptions.BusinessRuleException;
import com.ticketflow.eventservice.exceptions.ResourceNotFoundException;
import com.ticketflow.eventservice.repos.InventoryHoldRepo;
import com.ticketflow.eventservice.repos.TicketTierRepo;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class InventoryService {
    private final InventoryHoldRepo holds;
    private final TicketTierRepo tiers;
    public InventoryService(InventoryHoldRepo holds, TicketTierRepo tiers) {
        this.holds = holds;
        this.tiers = tiers;
    }
    public InventoryHoldResponse hold(UUID id, InventoryHoldRequest request) {
        InventoryHold existing = holds.findLocked(id).orElse(null);
        if (existing != null) {
            if (existing.getStatus() == InventoryHoldStatus.HELD
                    && !existing.getExpiresAt().isAfter(Instant.now())) {
                throw new BusinessRuleException("Hold has expired");
            }
            if (existing.getTier().getId().equals(request.tierId())
                    && existing.getQuantity() == request.quantity()
                    && existing.getStatus() == InventoryHoldStatus.HELD)
                return InventoryHoldResponse.from(existing);
            throw new BusinessRuleException("Hold id already used");
        }
        TicketTier tier = lockedTier(request.tierId());
        if (tier.getEvent().getStatus() != EventStatus.PUBLISHED)
            throw new BusinessRuleException("Event is not published");
        try { tier.reserve(request.quantity()); }
        catch (IllegalArgumentException ex) { throw new BusinessRuleException(ex.getMessage()); }
        return InventoryHoldResponse.from(holds.save(new InventoryHold(id, tier,
                request.quantity(), request.expiresAt())));
    }
    public InventoryHoldResponse confirm(UUID id) {
        InventoryHold hold = get(id);
        if (hold.getStatus() == InventoryHoldStatus.CONFIRMED) return InventoryHoldResponse.from(hold);
        if (hold.getStatus() != InventoryHoldStatus.HELD || !hold.getExpiresAt().isAfter(Instant.now()))
            throw new BusinessRuleException("Hold is no longer active");
        hold.confirm();
        return InventoryHoldResponse.from(hold);
    }
    public InventoryHoldResponse release(UUID id) {
        InventoryHold hold = get(id);
        if (hold.getStatus() == InventoryHoldStatus.RELEASED) return InventoryHoldResponse.from(hold);
        if (hold.getStatus() != InventoryHoldStatus.HELD)
            throw new BusinessRuleException("Confirmed hold cannot be released");
        TicketTier tier = lockedTier(hold.getTier().getId());
        tier.release(hold.getQuantity());
        hold.release();
        return InventoryHoldResponse.from(hold);
    }
    public int releaseExpired() {
        int released = 0;
        for (InventoryHold hold : holds.findByStatusAndExpiresAtBefore(InventoryHoldStatus.HELD, Instant.now())) {
            release(hold.getId());
            released++;
        }
        return released;
    }
    private InventoryHold get(UUID id) {
        return holds.findLocked(id).orElseThrow(() -> new ResourceNotFoundException("Hold", id));
    }
    private TicketTier lockedTier(UUID id) {
        return tiers.findLocked(id).orElseThrow(() -> new ResourceNotFoundException("Ticket tier", id));
    }
}
