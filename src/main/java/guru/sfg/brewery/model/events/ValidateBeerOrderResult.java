package guru.sfg.brewery.model.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by okostetskyi on 22.07.2022
 */
@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class ValidateBeerOrderResult implements Serializable {

    private static final long serialVersionUID = -7081626503712674358L;

    private UUID orderId;
    private boolean isValid;
}
