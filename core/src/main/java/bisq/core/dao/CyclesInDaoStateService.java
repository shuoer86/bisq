/*
 * This file is part of Bisq.
 *
 * Bisq is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bisq is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bisq. If not, see <http://www.gnu.org/licenses/>.
 */

package bisq.core.dao;

import bisq.core.dao.governance.period.CycleService;
import bisq.core.dao.state.DaoStateService;
import bisq.core.dao.state.model.governance.Cycle;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

/**
 * Utility methods for Cycle related methods.
 * As they might be called often we use caching.
 */
@Slf4j
@Singleton
public class CyclesInDaoStateService {
    private final DaoStateService daoStateService;
    private final CycleService cycleService;

    // Cached results
    private final Map<Integer, Cycle> cyclesByHeight = new HashMap<>();
    private final Map<Cycle, Integer> indexByCycle = new HashMap<>();
    private final Map<Integer, Cycle> cyclesByIndex = new HashMap<>();

    @Inject
    public CyclesInDaoStateService(DaoStateService daoStateService, CycleService cycleService) {
        this.daoStateService = daoStateService;
        this.cycleService = cycleService;
    }

    public int getCycleIndexAtChainHeight(int chainHeight) {
        return findCycleAtHeight(chainHeight)
                .map(cycleService::getCycleIndex)
                .orElse(-1);
    }

    public int getHeightOfFirstBlockOfPastCycle(int chainHeight, int numPastCycles) {
        return findCycleAtHeight(chainHeight)
                .map(cycle -> {
                    int cycleIndex = getIndexForCycle(cycle);
                    int targetIndex = cycleIndex - numPastCycles;
                    return getCycleAtIndex(targetIndex);
                })
                .map(Cycle::getHeightOfFirstBlock)
                .orElse(daoStateService.getGenesisBlockHeight());
    }

    private Cycle getCycleAtIndex(int index) {
        return Optional.ofNullable(cyclesByIndex.get(index))
                .orElseGet(() -> {
                    Cycle cycle = daoStateService.getCycleAtIndex(index);
                    cyclesByIndex.put(index, cycle);
                    return cycle;
                });
    }

    private int getIndexForCycle(Cycle cycle) {
        return Optional.ofNullable(indexByCycle.get(cycle))
                .orElseGet(() -> {
                    int index = cycleService.getCycleIndex(cycle);
                    indexByCycle.put(cycle, index);
                    return index;
                });
    }

    private Optional<Cycle> findCycleAtHeight(int chainHeight) {
        return Optional.ofNullable(cyclesByHeight.get(chainHeight))
                .or(() -> {
                    Optional<Cycle> optionalCycle = daoStateService.getCycle(chainHeight);
                    optionalCycle.ifPresent(cycle -> cyclesByHeight.put(chainHeight, cycle));
                    return optionalCycle;
                });
    }
}
